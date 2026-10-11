# Job Scheduler

## Compile
```bash
javac src/*.java
```

## Run
ลบ .log เก่า
```bash
Remove-Item log\* -Force
```

เริ่มทดสอบไฟล์
```bash
java -cp src Main workloads\jobs_standard.csv fcfs 3 1 2
```

ถ้า Terminal อยู่ใน `src` แล้ว:
```bash
java Main ..\workloads\jobs_standard.csv priority 3 1 2
```

## Arguments
- `workload.csv` — ไฟล์ workload
- `fcfs|priority` — Scheduling Policy
- `workers` — จำนวน Worker
- `printerPermits` — จำนวน Printer permits
- `databasePermits` — จำนวน Database permits

## Notes

All 5 arguments are required.

Priority 1 is the highest priority.

Workload must follow the required CSV format.

Logs are saved in the src/log/ directory.