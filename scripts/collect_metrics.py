#!/usr/bin/env python3
"""Собирает метрики качества проекта и печатает их в формате Markdown."""
import csv
import glob
import os
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

CCN_LIMIT = 10
DAYS = 30


def local(tag):
    """Имя тега без namespace."""
    return tag.rsplit("}", 1)[-1]


def run(cmd):
    return subprocess.run(cmd, capture_output=True, text=True)


def tests():
    files = glob.glob("build/test-results/*/TEST-*.xml")
    if not files:
        return ["Результаты тестов не найдены."]
    total = failed = skipped = 0
    seconds = 0.0
    for path in files:
        root = ET.parse(path).getroot()
        total += int(root.get("tests", 0))
        failed += int(root.get("failures", 0)) + int(root.get("errors", 0))
        skipped += int(root.get("skipped", 0))
        seconds += float(root.get("time", 0))
    return [
        f"Всего: **{total}**, упало: **{failed}**, пропущено: {skipped}",
        f"Время выполнения тестов: **{seconds:.1f} с**",
    ]


def coverage():
    path = "build/reports/jacoco/test/jacocoTestReport.xml"
    if not os.path.exists(path):
        return ["Отчёт JaCoCo не найден."]
    lines = []
    for c in ET.parse(path).getroot().findall("counter"):
        kind = c.get("type")
        if kind not in ("LINE", "BRANCH"):
            continue
        covered = int(c.get("covered"))
        total = covered + int(c.get("missed"))
        pct = 100 * covered / total if total else 0
        label = "строк" if kind == "LINE" else "ветвлений"
        lines.append(f"Покрытие {label}: **{pct:.1f}%** ({covered} из {total})")
    return lines


def lint():
    path = "build/reports/checkstyle/main.xml"
    if not os.path.exists(path):
        return ["Отчёт Checkstyle не найден (шаг линтера не выполнился)."]
    by_severity = {}
    for el in ET.parse(path).getroot().iter():
        if local(el.tag) == "error":
            sev = el.get("severity", "error")
            by_severity[sev] = by_severity.get(sev, 0) + 1
    lines = [f"Всего нарушений: **{sum(by_severity.values())}**"]
    for sev, n in sorted(by_severity.items()):
        lines.append(f"- {sev}: {n}")
    return lines


def complexity():
    r = run([sys.executable, "-m", "lizard", "-l", "java", "--csv", "src/main/java"])
    funcs = []
    for row in csv.reader(r.stdout.splitlines()):
        # колонки Lizard: NLOC, CCN, token, PARAM, length, location, file, function, ...
        if len(row) >= 8 and row[1].isdigit():
            funcs.append((int(row[1]), row[7], row[6]))
    if not funcs:
        return ["Lizard не вернул данных: " + (r.stderr.strip() or "функции не найдены")]
    avg = sum(f[0] for f in funcs) / len(funcs)
    worst = sorted(funcs, reverse=True)[:5]
    over = sum(1 for f in funcs if f[0] > CCN_LIMIT)
    lines = [
        f"Проанализировано методов: {len(funcs)}",
        f"Средняя сложность: **{avg:.2f}**",
        f"Максимальная: **{worst[0][0]}** (`{worst[0][1]}`)",
        f"Методов со сложностью больше {CCN_LIMIT}: **{over}**",
        "",
        "Самые сложные:",
    ]
    for ccn, name, path in worst:
        lines.append(f"- {ccn}: `{name}` ({os.path.basename(path)})")
    return lines


def commits():
    since = f"--since={DAYS} days ago"
    n = int(run(["git", "rev-list", "--count", since, "HEAD"]).stdout.strip() or 0)
    log = run(["git", "log", since, "--no-merges", "--numstat", "--pretty=format:@@"]).stdout
    sizes = []
    for line in log.splitlines():
        if line == "@@":
            sizes.append(0)
            continue
        parts = line.split("\t")
        if len(parts) == 3 and parts[0].isdigit() and parts[1].isdigit() and sizes:
            sizes[-1] += int(parts[0]) + int(parts[1])
    lines = [f"Коммитов за {DAYS} дней: **{n}** (в среднем {n / (DAYS / 7):.1f} в неделю)"]
    if sizes:
        lines.append(
            f"Средний размер коммита: **{sum(sizes) / len(sizes):.0f}** изменённых строк, "
            f"максимальный: {max(sizes)}"
        )
    last = run(["git", "diff", "--shortstat", "HEAD~1", "HEAD"]).stdout.strip()
    lines.append("Последний коммит: " + (last or "нет данных"))
    return lines


def duration():
    start = os.environ.get("PIPELINE_START")
    if not start:
        return ["Время старта не передано (локальный запуск)."]
    s = int(time.time() - int(start))
    return [
        f"Длительность пайплайна: **{s // 60} мин {s % 60} с**",
        "(без запуска раннера; полное время указано на странице запуска)",
    ]


SECTIONS = [
    ("Тесты", tests),
    ("Покрытие кода", coverage),
    ("Линтер (Checkstyle)", lint),
    ("Цикломатическая сложность (Lizard)", complexity),
    ("Частота и размер коммитов", commits),
    ("Длительность пайплайна", duration),
]


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    print("## Метрики качества\n")
    for title, fn in SECTIONS:
        print(f"### {title}\n")
        try:
            lines = fn()
        except Exception as e:  # одна сломанная метрика не должна ронять остальные
            lines = [f"Не удалось собрать метрику: {e}"]
        print("\n".join(lines) + "\n")


main()