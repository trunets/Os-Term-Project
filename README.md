# Mini Job Scheduler & Shared Resource Manager

โปรแกรมจำลองการรับงานตามเวลา จัดลำดับแบบ FCFS หรือ Priority และให้ Worker หลายตัวทำงานร่วมกัน โดยควบคุม Printer และ Database ด้วย Semaphore

## Compile

```powershell
javac -encoding UTF-8 java\*.java
```

## Run

รันจากโฟลเดอร์ `java` เพื่อให้ classpath ถูกต้อง ตัวอย่าง:

```powershell
cd java
java Main ..\csv\jobs_standard.csv priority 3 1 2
```

รูปแบบคำสั่ง:

```text
java Main <workload.csv> <fcfs|priority> <workers> <printerPermits> <databasePermits>
```

- `workload.csv` คือไฟล์ CSV ที่มีหัวตาราง `id,arrivalMs,priority,workMs,resource,resourceMs`
- `fcfs|priority` เลือกนโยบายจัดลำดับงาน
- `workers`, `printerPermits` และ `databasePermits` ต้องเป็นจำนวนเต็มบวก

โปรแกรมใช้ poison pill เพื่อหยุด Worker หลังงานจริงทั้งหมดถูกส่งเข้าคิวแล้ว และ Main จะ interrupt Monitor หลัง Worker ทุกตัวจบ จึงไม่ใช้ `Thread.stop()` หรือ `System.exit()`.
