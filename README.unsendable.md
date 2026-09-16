# ------------ ยังไม่ได้อ่าน ------------

# Mini Job Scheduler & Shared Resource Manager

Java Multithreading: Thread, Synchronization, Semaphore และ Scheduling

## 1. Project Overview

โครงงานนี้เป็นโปรแกรมจำลองระบบรับงาน (Job) ที่ Job เข้ามาตามเวลา มี Scheduler จัดลำดับงาน และมี Worker Thread หลายตัวรับงานไปทำพร้อมกัน

งานบางชนิดต้องใช้ Shared Resource ที่มีจำนวนจำกัด ได้แก่

* `PRINTER`
* `DATABASE`

โดยใช้ `Semaphore` ควบคุมจำนวน Job ที่สามารถใช้ Resource พร้อมกันได้

โปรแกรมนี้เป็น Simulation ไม่ใช่ Kernel หรือ CPU Scheduler จริง และไม่เชื่อมต่ออุปกรณ์หรือฐานข้อมูลจริง

จุดประสงค์คือเพื่อสังเกตและอธิบายพฤติกรรมของ Thread, Queue, Scheduling และ Shared Resource จากการรันจริง

---

## 2. System Architecture

```text
JobGenerator Thread
        |
        v
arrivalQueue (BlockingQueue)
        |
        v
Scheduler Thread
        |
        v
SchedulingPolicy / Ready Queue
        |
        +---- FCFS
        |
        +---- Priority
        |
        v
Worker Threads
        |
        +--> ResourceManager
        |       |
        |       +--> PRINTER Semaphore
        |       |
        |       +--> DATABASE Semaphore
        |
        +--> Statistics
        |
        +--> Logger

Monitor Thread
        |
        v
System Status
```

### Main / Controller

รับผิดชอบ:

* สร้าง Object ที่จำเป็น
* เริ่มระบบ
* รอให้ระบบทำงานเสร็จ
* สั่ง Shutdown
* แสดง Summary

### JobGenerator

รับผิดชอบ:

* อ่าน Job จาก Workload
* ปล่อย Job ตาม `arrivalMs`
* เพิ่ม Job ลง `arrivalQueue`
* แจ้งระบบเมื่อส่ง Job ครบ

### Scheduler

รับผิดชอบ:

* รับ Job จาก `arrivalQueue`
* เพิ่ม Job เข้า Ready Queue
* เลือก Scheduling Policy
* รองรับ FCFS และ Priority

**JobGenerator ห้ามส่ง Job เข้า Ready Queue โดยตรง**

### Worker

รับผิดชอบ:

* รับ Job จาก Ready Queue
* ประมวลผล Job
* ใช้ Shared Resource เมื่อจำเป็น
* อัปเดต Statistics
* จบการทำงานอย่างถูกต้องเมื่อไม่มี Job เหลือ

### ResourceManager

รับผิดชอบ:

* จัดการ PRINTER
* จัดการ DATABASE
* ใช้ Semaphore
* ใช้ ResourceManager Object เดียวร่วมกันทั้งระบบ
* คืน permit อย่างปลอดภัย

### Monitor

รับผิดชอบ:

* อ่านสถานะระบบประมาณทุก 1,000 ms
* แสดงจำนวน Ready
* แสดงจำนวน Running
* แสดงจำนวน Completed
* แสดงสถานะ Resource
* อ่านข้อมูลแบบ Thread-safe

---

## 3. Job Data

Workload CSV มี Field ดังนี้:

| Field        | Description                                   |
| ------------ | --------------------------------------------- |
| `id`         | รหัส Job ต้องไม่ซ้ำกัน                        |
| `arrivalMs`  | เวลาที่ Job เข้าระบบ นับจาก `simulationStart` |
| `priority`   | `1` = priority สูงสุด                         |
| `workMs`     | เวลางานหลัก                                   |
| `resource`   | `NONE`, `PRINTER`, `DATABASE`                 |
| `resourceMs` | เวลาที่ถือ Resource                           |

ตัวอย่าง:

```csv
id,arrivalMs,priority,workMs,resource,resourceMs
J01,0,5,2500,NONE,0
J02,100,5,2400,PRINTER,1200
```

Job หนึ่งตัวใช้ Shared Resource ได้ไม่เกิน 1 ประเภทในส่วนบังคับ

Workload ต้องอ่านจากไฟล์ภายนอก ห้าม Hard-code Job ในโปรแกรม เนื่องจากอาจมีการใช้ Workload อื่นในวัน Demo

---

## 4. Scheduling

### FCFS

FCFS เลือก Job ที่อยู่ในสถานะ READY ก่อน

เป็น Non-preemptive ระดับ Job:

* เมื่อ Worker รับ Job แล้ว
* Job จะทำจนจบ
* ไม่ถูกดึงกลับกลางคัน

### Priority

Priority ใช้กติกา:

```text
priority = 1 -> สูงสุด
priority มากขึ้น -> priority ต่ำลง
```

เป็น Non-preemptive ระดับ Job เช่นเดียวกับ FCFS

### Tie-break

เมื่อ Priority เท่ากัน ต้องใช้กติกาที่กำหนดจากข้อมูลของ Job เช่น:

* Arrival order
* Sequence number
* Job ID

Tie-break ต้องไม่ขึ้นกับว่า Thread ใดเข้าถึง Queue ก่อน

---

## 5. Shared Resource

### PRINTER

Default:

```text
1 permit
```

หมายถึงมี Logical Printer 1 ตัวที่สามารถถูกใช้งานพร้อมกันได้ 1 Job

### DATABASE

Default:

```text
2 permits
```

หมายถึงสามารถมี Job ใช้ DATABASE พร้อมกันได้สูงสุด 2 Job

### Semaphore

Semaphore ต้อง:

* ใช้ร่วมกันผ่าน ResourceManager Object เดียว
* สามารถกำหนดจำนวน permit จาก Command Line
* ใช้ `fair=true`
* Release permit อย่างปลอดภัย
* ไม่ทำให้ permit สูญหายหรือค้าง

ทุก `acquire()` ต้องมีการรับประกันว่าจะ `release()` แม้เกิด Exception หรือ Interrupt เช่นใช้ `try/finally`

---

## 6. Worker Processing

Worker ต้องทำงานตามลำดับ:

```text
1. รับ Job
2. บันทึก startTime
3. sleep(workMs)
4. ถ้ามี Resource:
   - บันทึกเวลาเริ่มรอ
   - acquire Semaphore
   - คำนวณ Resource Wait Time
   - sleep(resourceMs)
   - release Semaphore
5. บันทึก completionTime
6. Update Statistics
```

Job State ที่ใช้ในระบบ:

```text
ARRIVED
READY
RUNNING
WAITING_RESOURCE
COMPLETED
```

Job State ไม่จำเป็นต้องตรงกับ `Java Thread.State` แบบหนึ่งต่อหนึ่ง

---

## 7. Synchronization

ข้อมูลที่หลาย Thread ใช้ร่วมกันต้องมีการป้องกัน เช่น:

* `runningJobs`
* `ready`
* `completed`
* Statistics
* Shared Queue
* Resource state

สามารถใช้:

```text
synchronized
Lock
Atomic classes
```

หรือวิธี Thread-safe ที่เหมาะสม

ต้องไม่มี Race Condition และ Monitor ต้องสามารถอ่านข้อมูลได้อย่างปลอดภัยในขณะที่ Worker กำลังแก้ไขข้อมูล

---

## 8. Metrics

### Waiting Time

```text
Waiting Time = startTime - actualArrivalTime
```

รายงานค่าเฉลี่ยจาก Job ทุกตัว

### Turnaround Time

```text
Turnaround Time = completionTime - actualArrivalTime
```

รายงานค่าเฉลี่ยจาก Job ทุกตัว

### Resource Wait Time

เวลาตั้งแต่เริ่มรอ Semaphore จน `acquire()` สำเร็จ

สำหรับ:

```text
resource = NONE
```

ต้องเป็น:

```text
Resource Wait Time = 0
```

Average Resource Wait Time ให้คำนวณเฉพาะ Job ที่ขอ PRINTER หรือ DATABASE

### Throughput

```text
Throughput =
จำนวน Job ที่เสร็จ / เวลารวมของ Simulation
```

หน่วย:

```text
jobs/second
```

### ตรวจสอบความถูกต้อง

สำหรับแต่ละ Job:

```text
Turnaround Time
=
Waiting Time
+ workMs
+ Resource Wait Time
+ resourceMs
```

สำหรับ `NONE`:

```text
Resource Wait Time = 0
resourceMs = 0
```

สมการนี้ใช้ตรวจเป็นราย Job ไม่ใช่นำค่า Average ของแต่ละ Column มาบวกกัน

---

## 9. Logging

เวลาใน Log นับจาก `simulationStart`

ตัวอย่าง:

```text
[0000 ms] [JobGenerator] J01 ARRIVED priority=5
[0004 ms] [Scheduler] J01 READY
[0012 ms] [Worker-1] J01 START
[2420 ms] [Worker-2] J02 WAIT PRINTER
[2421 ms] [Worker-2] J02 ACQUIRE PRINTER
[3622 ms] [Worker-2] J02 RELEASE PRINTER
[3623 ms] [Worker-2] J02 COMPLETE
```

Log ต้องมี:

```text
ARRIVED
READY
START
WAIT
ACQUIRE
RELEASE
COMPLETE
Monitor status
```

ทุกบรรทัดต้องมีชื่อ Thread

การรันแต่ละครั้งอาจมี Timestamp หรือลำดับ Event แตกต่างกันเล็กน้อย เนื่องจาก OS/Java สามารถ Schedule Thread แตกต่างกันในแต่ละครั้ง แต่กติกาของ Scheduling และแนวโน้มของผลต้องสามารถอธิบายได้

---

## 10. Command Line

รูปแบบการรัน:

```bash
java Main <workload.csv> <fcfs|priority> <workers> <printerPermits> <databasePermits>
```

ตัวอย่าง:

```bash
java Main jobs_standard.csv priority 3 1 2
```

Arguments:

```text
1. workload.csv
2. fcfs หรือ priority
3. workers
4. printerPermits
5. databasePermits
```

หาก Argument ไม่ครบหรือค่าผิด ต้องแสดง:

```text
Usage: java Main <workload.csv> <fcfs|priority> <workers> <printerPermits> <databasePermits>
```

และต้องจบก่อนเริ่ม Thread ใด ๆ

ห้ามใช้ Default Value แบบเงียบ ๆ โดยไม่แจ้งผู้ใช้

---

## 11. Graceful Shutdown

ระบบต้องสามารถ Shutdown ได้โดย:

```text
JobGenerator ส่งงานครบ
        ↓
Scheduler ประมวลผล Job ที่เหลือ
        ↓
Worker ทำ Job จนครบ
        ↓
ระบบตรวจพบว่างานทั้งหมดเสร็จ
        ↓
หยุด Worker
        ↓
หยุด Monitor
        ↓
Main แสดง Summary
        ↓
JVM จบเอง
```

สามารถใช้วิธี เช่น:

* Poison Pill
* CountDownLatch
* Counter สำหรับ Job ที่ยังไม่เสร็จ
* วิธีอื่นที่ปลอดภัย

ต้องไม่เกิด:

* Main จบแต่ JVM ยังไม่หยุดเพราะมี Thread ค้าง
* Worker หยุดก่อน Job สุดท้ายเสร็จ
* Semaphore permit สูญหาย
* Semaphore permit ค้าง
* Worker ค้างใน `takeNextJob()`
* Monitor ค้าง
* ใช้ `System.exit()` เพื่อหลบปัญหา Shutdown

---

## 12. Forbidden Techniques

ห้ามใช้:

```java
Thread.stop()
Thread.suspend()
Thread.resume()
```

และห้าม:

* Busy waiting
* ใช้ `sleep()` เพื่อเดาว่า Thread อื่นทำงานเสร็จแล้ว
* ใช้ `System.exit()` เพื่อแก้ปัญหา Shutdown

`Thread.sleep(workMs)` และ `Thread.sleep(resourceMs)` ใช้ได้เพราะเป็นส่วนหนึ่งของ Simulation

---

## 13. Workload Files

### jobs_standard.csv

ใช้สำหรับ:

* Scheduling
* Worker count
* FCFS/Priority
* Resource contention

### jobs_printer.csv

ใช้สำหรับ:

* ทดสอบ Printer bottleneck
* เปรียบเทียบ Printer permits = 1 และ 2

### jobs_db.csv

ใช้สำหรับ:

* ทดสอบ DATABASE permits
* ตรวจว่า permits = 2 สามารถให้สูงสุด 2 Job ใช้ DATABASE พร้อมกัน

### jobs_same_priority.csv

ใช้สำหรับ:

* ทดสอบ Priority tie-break
* ทุก Job มี Priority เท่ากัน

### jobs_single.csv

ใช้สำหรับ:

* ทดสอบ Job เดียว
* ทดสอบ Graceful Shutdown
* ตรวจว่าโปรแกรมไม่ค้าง

ห้ามผูก Logic กับ ID หรือจำนวน Job ของ Workload ตัวอย่าง

---

## 14. Required Experiments

ต้องใช้ Code ชุดเดียวกันในการทดลองทั้งหมด และเปลี่ยนเฉพาะค่าการ Run กับ Workload

### Experiment A — Scheduling

```text
jobs_standard.csv
workers = 3
printer = 1
database = 2
```

เปรียบเทียบ:

```text
FCFS
Priority
```

สังเกต:

* Average Waiting Time
* Average Turnaround Time
* START order
* Job ที่ได้ประโยชน์
* Job ที่เสียประโยชน์

### Experiment B — Worker Count

```text
jobs_standard.csv
Priority
printer = 1
database = 2
workers = 1, 3, 5
```

สังเกต:

* Throughput
* Average Resource Wait
* จุดที่เพิ่ม Worker แล้วได้ประโยชน์น้อยลง

### Experiment C — Printer Bottleneck

```text
jobs_printer.csv
Priority
workers = 3
database = 2
```

เปรียบเทียบ:

```text
printer = 1
printer = 2
```

สังเกต:

* Average Resource Wait
* Throughput
* Bottleneck ของระบบ

### Experiment Repeat

รันซ้ำ:

```text
jobs_standard.csv
Priority
workers = 3
printer = 1
database = 2
```

โดยไม่เปลี่ยนค่า

เปรียบเทียบ:

* Timestamp
* Event order
* Metrics
* ความแตกต่างจาก Nondeterminism

---

## 15. Result Table

ต้องมีอย่างน้อย 7 แถว:

| # | Workload / Policy            | Workers | Printer | DB | avg WT | avg TAT | Throughput | avg RW |
| - | ---------------------------- | ------: | ------: | -: | -----: | ------: | ---------: | -----: |
| 1 | standard / FCFS              |       3 |       1 |  2 |        |         |            |        |
| 2 | standard / Priority          |       3 |       1 |  2 |        |         |            |        |
| 3 | standard / Priority          |       1 |       1 |  2 |        |         |            |        |
| 4 | standard / Priority          |       5 |       1 |  2 |        |         |            |        |
| 5 | printer / Priority           |       3 |       1 |  2 |        |         |            |        |
| 6 | printer / Priority           |       3 |       2 |  2 |        |         |            |        |
| 7 | standard / Priority (repeat) |       3 |       1 |  2 |        |         |            |        |

รูปแบบตัวเลข:

```text
avg WT       -> integer ms
avg TAT      -> integer ms
avg RW       -> integer ms
Throughput   -> อย่างน้อย 2 decimal places
```

แถว 2 และ 7 ต้องอ้างอิง Raw Log ไม่ใช่ดูเฉพาะค่าเฉลี่ย

---

## 16. Required Questions

### 1. Worker Count

เพิ่ม Worker จาก 3 เป็น 5 แล้ว Throughput เพิ่มตามสัดส่วนหรือไม่ และ Average Resource Wait เปลี่ยนอย่างไร เพราะอะไร?

ต้องอ้างอิงผลจากการทดลองจริง

### 2. FCFS vs Priority

FCFS กับ Priority ทำให้ Job กลุ่มใดได้ประโยชน์หรือเสียประโยชน์จาก `jobs_standard.csv`?

ต้องอ้างอิง:

* Waiting Time
* START order
* Raw Log

และอธิบายว่าทำไม Priority อาจมี Throughput ต่ำกว่า FCFS แม้ Average Waiting Time จะดีกว่า

### 3. Printer Permit

เมื่อเพิ่ม Printer permit จาก 1 เป็น 2:

* Average Resource Wait เปลี่ยนอย่างไร?
* Throughput เปลี่ยนอย่างไร?
* Bottleneck ย้ายไปอยู่ที่ใด?

ต้องอ้างอิงผลจากการทดลองจริง

### 4. Repeat Run

ทำไม Row 2 และ Row 7 ซึ่งใช้ Workload และ Configuration เหมือนกัน จึงอาจมี:

* Timestamp ต่างกัน
* Event order ต่างกัน
* Metrics ต่างกันเล็กน้อย

ต้องอธิบายเรื่อง Thread Scheduling และ Nondeterminism

---

## 17. Submission Checklist

### Code

* [ ] Java source code ทั้งหมด
* [ ] JobGenerator
* [ ] Scheduler
* [ ] Ready Queue
* [ ] FCFS
* [ ] Priority
* [ ] Tie-break
* [ ] Worker
* [ ] `processJob()`
* [ ] ResourceManager
* [ ] Semaphore
* [ ] Statistics
* [ ] Logger
* [ ] Monitor
* [ ] Synchronization
* [ ] Graceful Shutdown
* [ ] Interrupt handling

### README.md

ต้องมี:

* [ ] วิธี Compile
* [ ] วิธี Run
* [ ] Command-line arguments
* [ ] ข้อจำกัดที่ควรรู้

### Raw Logs

* [ ] Log ของแต่ละชุดการ Run ที่กำหนด
* [ ] สามารถ Redirect ลงไฟล์ได้
* [ ] ไม่ต้องจัดหน้าเป็นรายงาน

### Result

* [ ] ตารางผลลัพธ์ 1 หน้า
* [ ] คำตอบ 4 ข้อ
* [ ] ใช้ค่าจากการ Run จริง

### AI Usage

* [ ] ระบุ AI Tool ที่ใช้
* [ ] ระบุว่า AI ช่วยเรื่องใด
* [ ] ระบุอย่างน้อย 1 จุดที่กลุ่มแก้เอง
* [ ] ระบุอย่างน้อย 1 จุดที่กลุ่มตรวจสอบคำตอบของ AI เอง

### ไม่ต้องส่ง

* [ ] รายงาน 5–8 หน้า
* [ ] System diagram เป็นไฟล์
* [ ] Screenshot
* [ ] `.class`
* [ ] IDE cache
* [ ] `target/`
* [ ] `build/`

---

## 18. Demo Checklist

สมาชิกทุกคนต้องสามารถ:

* [ ] อธิบาย Code ทั้งระบบ
* [ ] อธิบาย Thread ทุกตัว
* [ ] อธิบาย Shared Data
* [ ] อธิบาย Synchronization
* [ ] อธิบาย Semaphore
* [ ] อธิบาย FCFS
* [ ] อธิบาย Priority
* [ ] อธิบาย Tie-break
* [ ] อธิบาย Metrics
* [ ] อธิบาย Shutdown
* [ ] แก้ไข Code ต่อหน้าได้
* [ ] ทดสอบ Code ต่อหน้าได้
* [ ] ตอบคำถามรายบุคคลได้

อาจารย์อาจให้:

* เปลี่ยนจำนวน Worker
* เปลี่ยน Semaphore permits
* ชี้ `runningJobs`
* ถามว่าจะเกิดอะไรหากไม่มี synchronization
* ตรวจ FCFS/Priority
* ตรวจ Tie-break
* ตรวจ Shutdown
* Interrupt Worker ระหว่างรอ Resource
* ถามเรื่อง `Thread.State`
* ตรวจสมการ TAT
* ถามการใช้ Generative AI

---

## 19. Required Test Cases

### Single Job

```text
jobs_single.csv
```

* [ ] Job ทำงานสำเร็จ
* [ ] Shutdown สำเร็จ
* [ ] ไม่มี Thread ค้าง

### Standard

```text
jobs_standard.csv
```

* [ ] Worker หลายตัวทำงานพร้อมกัน
* [ ] FCFS ทำงานถูกต้อง
* [ ] Priority ทำงานถูกต้อง
* [ ] START order แตกต่างตาม Policy

### Printer

```text
jobs_printer.csv
printerPermits = 1
```

* [ ] ไม่มี 2 Job ใช้ PRINTER พร้อมกัน

### Database

```text
jobs_db.csv
databasePermits = 2
```

* [ ] DATABASE ถูกใช้งานพร้อมกันได้สูงสุด 2 Job

### Same Priority

```text
jobs_same_priority.csv
```

* [ ] Tie-break ทำงานตามกติกา
* [ ] ไม่ขึ้นกับ Thread ที่เข้าถึง Queue ก่อน

### Interrupt

* [ ] Interrupt Worker ระหว่างรอ Resource
* [ ] โปรแกรมไม่ค้าง
* [ ] Permit ไม่สูญหาย
* [ ] Resource ถูกคืนอย่างถูกต้อง

### Final Shutdown

* [ ] Summary แสดงครบ
* [ ] JVM ปิดเอง
* [ ] Worker ไม่ค้าง
* [ ] Monitor ไม่ค้าง
* [ ] ไม่มี Job ค้าง
* [ ] ไม่มี Semaphore permit ค้าง

---

## 20. Starter Code

อาจารย์อาจแจก:

```text
WorkloadLoader.java
ProjectLogger.java
class/interface พื้นฐาน
```

`WorkloadLoader` รับผิดชอบ:

* อ่าน CSV
* ตรวจสอบความถูกต้องของ CSV

นักศึกษาไม่จำเป็นต้องเขียน CSV Reader เอง

ส่วนที่นักศึกษาต้องออกแบบและเขียนเอง:

* Ready Queue
* FCFS
* Priority
* Scheduler Thread
* Tie-break
* Worker lifecycle
* `processJob()`
* ResourceManager
* Semaphore
* Safe resource release
* Per-Job metrics
* Statistics
* `runningJobs`
* Ready/Completed counters
* Monitor snapshot
* Graceful Shutdown
* Interrupt handling
* Additional testing
* Design explanation สำหรับ Demo

---

## 21. Bonus / Extension

ส่วนนี้ไม่ใช่ข้อบังคับ และควรทำหลังจากส่วนหลักทำงานครบแล้ว

* [ ] Aging เพื่อลด Starvation ของ Priority Scheduling
* [ ] MLFQ

  * [ ] หลาย Queue
  * [ ] Time Quantum
  * [ ] `remainingWorkMs`
  * [ ] Requeue
* [ ] Dynamic Worker

  * [ ] เพิ่ม Worker ตาม Queue Length
  * [ ] ลด Worker ตาม Queue Length
* [ ] Job Cancellation
* [ ] Resource Timeout ด้วย `tryAcquire()`
* [ ] Virtual Thread comparison สำหรับ Workload ที่มีการรอจำนวนมาก
* [ ] Deadlock Challenge

  * [ ] Job ใช้ Resource มากกว่า 1 ชนิด
  * [ ] ออกแบบเพื่อป้องกัน Deadlock

---

## 22. Final Checklist

ก่อนส่งงาน ตรวจให้ครบ:

* [ ] JobGenerator Thread
* [ ] Arrival BlockingQueue
* [ ] Scheduler Thread
* [ ] Ready Queue
* [ ] FCFS
* [ ] Priority
* [ ] Tie-break
* [ ] Worker Threads
* [ ] ResourceManager
* [ ] PRINTER Semaphore
* [ ] DATABASE Semaphore
* [ ] Fair Semaphore
* [ ] Statistics
* [ ] Logger
* [ ] Monitor Thread
* [ ] Thread-safe shared data
* [ ] Waiting Time
* [ ] Turnaround Time
* [ ] Resource Wait Time
* [ ] Throughput
* [ ] TAT consistency check
* [ ] Command-line arguments ครบ 5 ตัว
* [ ] Usage validation
* [ ] External workload
* [ ] Graceful Shutdown
* [ ] Interrupt handling
* [ ] ไม่มี busy waiting
* [ ] ไม่มี forbidden Thread API
* [ ] ไม่มี `System.exit()` สำหรับแก้ shutdown
* [ ] Raw Logs
* [ ] Experiment A
* [ ] Experiment B
* [ ] Experiment C
* [ ] Repeat Experiment
* [ ] Result Table 7 rows
* [ ] ตอบคำถาม 4 ข้อ
* [ ] AI Usage Record
* [ ] Test Cases ครบ
* [ ] ทุกสมาชิกอธิบาย Code ได้
* [ ] ทุกสมาชิกพร้อม Demo
* [ ] ไม่มี `.class`
* [ ] ไม่มี IDE cache
* [ ] ไม่มี `target/`
* [ ] ไม่มี `build/`

---

## 23. Important Requirement

โปรแกรมที่ “รันได้” เพียงอย่างเดียวไม่เพียงพอสำหรับคะแนนเต็ม

สมาชิกทุกคนต้องสามารถอธิบายได้ว่า:

```text
Thread แต่ละตัวทำอะไร
ข้อมูลใดถูกใช้ร่วมกัน
ป้องกัน Race Condition อย่างไร
Semaphore ทำงานอย่างไร
Scheduling ทำงานอย่างไร
Metrics แต่ละตัวหมายถึงอะไร
Shutdown ทำงานอย่างไร
AI ถูกใช้ช่วยส่วนใด
และกลุ่มตรวจสอบ/แก้ไขสิ่งที่ AI สร้างอย่างไร
```
