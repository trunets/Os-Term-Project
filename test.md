# OS Term Project — Demo Q&A

### เปลี่ยนจำนวน Worker หรือจำนวนสิทธิ์ของ Semaphore แล้วระบบเปลี่ยนอย่างไร?

**Answer:**  
เพิ่ม Worker หรือ Semaphore → งานรอคิวน้อยลงและทำงานเร็วขึ้น; ลด → งานรอมากขึ้นและช้าลง

### ชี้ข้อมูลร่วมที่หลาย Thread แก้ไขได้ และถ้าเอาการป้องกันออกจะเกิดอะไรขึ้น?

**Answer:**  
`runningJobs` และ `Statistics` เป็นข้อมูลที่หลาย Thread แก้ไขร่วมกัน หากไม่มีการป้องกัน อาจเกิด Race Condition ทำให้ค่าผิดพลาดหรือข้อมูลสูญหาย

### อธิบาย FCFS/Priority และกติกา Tie-break จากโค้ดจริง

**Answer:**  
FCFS ใช้ `LinkedList` เพราะใช้เป็น FIFO ได้ ส่วน Priority เปรียบเทียบค่า Priority โดยเลขที่ต่ำกว่าจะอยู่ก่อน หาก Priority เท่ากัน จะใช้ `sequenceNumber` เป็น Tie-break เพื่อเลือกงานที่มาก่อน

```text
FCFS     → งานที่เข้าคิวก่อน ทำก่อน
Priority → Priority เลขต่ำ ทำก่อน
Tie-break → sequenceNumber น้อยกว่า ทำก่อน
```

### ใช้ Semaphore/Scheduling อย่างไร?

**Answer:**  
ใช้ FCFS หรือ Priority ใน `ReadyQueue` เพื่อกำหนดลำดับงาน และใช้ Semaphore จำกัดจำนวน Worker ที่ใช้ Printer/Database พร้อมกัน

### เปรียบเทียบ Thread.State ตอนรอ Semaphore กับตอน `Thread.sleep(resourceMs)`

**Answer:**  
`Semaphore.acquire()` คือการรอ Resource/permit ส่วน `Thread.sleep(resourceMs)` จะอยู่ในสถานะ `TIMED_WAITING` เพื่อจำลองเวลาการใช้ Resource

### ถ้า Worker ถูก interrupt หลัง acquire resource จะป้องกันไม่ให้ permit หายได้อย่างไร?

**Answer:**  
หลัง `acquire()` ใช้ `try-finally` ครอบส่วนที่ใช้ Resource และเรียก `release()` ใน `finally` เพื่อให้ permit ถูกคืนแม้เกิด interrupt หรือ exception

```java
resource.acquire();

try {
    // ใช้ Resource
} finally {
    resource.release();
}
```

### ใช้สมการ TAT ตรวจสอบการทำงานอย่างไร?

```text
TAT = WT + workMs + Resource Wait + resourceMs
```

**Answer:**  
ถ้าค่าไม่ตรงกับ Timestamp ให้ตรวจจุดที่บันทึก `arrival`, `start`, `resource wait`, `resource acquire/release` และ `completion` ว่าบันทึกผิดช่วงเวลาหรือไม่

```text
arrival
  ↓
start
  ↓
workMs
  ↓
resource wait
  ↓
acquire
  ↓
resourceMs
  ↓
release
  ↓
completion
```

จุดที่เกี่ยวข้องในโค้ด:

```text
arrival                → JobGenerator.java
start                  → Worker.java
resource wait          → Worker.java
resource acquire/release → ResourceManager.java
completion             → Worker.java
WT / TAT / Resource Wait → Statistics.java
```

ถ้าจะไล่ดู Timestamp ให้เริ่มจาก `Job.java` → `Worker.java` → `Statistics.java`

### Generative AI ช่วยสร้างส่วนใด และกลุ่มตรวจสอบอย่างไร?

**Answer:**  
Generative AI ช่วยสร้างโครงสร้างและโค้ดบางส่วน เช่น Thread, Scheduling และ Resource Management จากนั้นกลุ่มตรวจสอบด้วยการอ่านโค้ด ทดสอบการทำงาน และเปรียบเทียบผลลัพธ์กับข้อกำหนดและสมการของโจทย์

### อธิบายได้ว่า Thread แต่ละตัวทำอะไร

**Answer:**

```text
JobGenerator → สร้างและส่ง Job
Scheduler    → รับ Job และจัดเข้า ReadyQueue
Worker       → หยิบ Job และทำงาน
Monitor      → แสดงสถานะระบบ
Statistics   → เก็บและคำนวณผลการวัด
```

### ข้อมูลใดใช้ร่วมกัน?

**Answer:**  
`ReadyQueue`, `Statistics`, สถานะของ Resource และข้อมูล `Job` ที่ถูกแชร์ เป็นข้อมูลที่หลาย Thread เข้าถึงร่วมกัน

### ป้องกันการชนกันอย่างไร?

**Answer:**  
ใช้ `synchronized`, thread-safe queue และ `Semaphore` เพื่อป้องกัน Race Condition และควบคุมการเข้าถึง Resource

### Poison Pill คืออะไร และใช้ทำอะไร?

**Answer:**  
Poison Pill คือ Job พิเศษที่ใช้เป็นสัญญาณบอก Worker ว่า **ไม่มีงานจริงเหลือแล้วและให้หยุดทำงาน**

ตัวอย่างแนวคิด:

```text
Scheduler
   ↓
ReadyQueue
   ↓
Worker → รับ Job จริง → ทำงานต่อ
Worker → รับ Poison Pill → หยุด
```

ข้อดีคือ Worker สามารถหยุดตัวเองอย่างเป็นระเบียบโดยไม่ต้องบังคับฆ่า Thread

ถ้ามี Worker หลายตัว โดยทั่วไปต้องมี Poison Pill ให้เพียงพอกับจำนวน Worker เช่น

```text
3 Workers
↓
Poison Pill 3 ตัว
↓
Worker แต่ละตัวรับ 1 ตัวแล้วหยุด
```

### Shutdown แบบ `interrupt()` ต่างจาก Poison Pill อย่างไร?

**Answer:**

```text
interrupt()
→ ส่งสัญญาณให้ Thread หยุด/ยกเลิกการรอ

Poison Pill
→ ส่ง Job พิเศษผ่าน Queue เพื่อบอก Worker ว่า "ไม่มีงานแล้ว ให้หยุด"
```

Poison Pill เหมาะกับการหยุด Worker ที่กำลังรอ `ReadyQueue.take()` เพราะ Worker สามารถตื่นขึ้นมา รับ Poison Pill และออกจาก loop ได้

### Workflow ของระบบ

```text
Main
 │
 ├── ResourceManager
 ├── ReadyQueue
 ├── Statistics
 ├── Workers
 ├── Scheduler
 ├── Monitor
 └── JobGenerator
        │
        ▼
   JobGenerator
   สร้าง Job + Arrival
        │
        ▼
    Scheduler
    ส่ง Job เข้า ReadyQueue
        │
        ▼
    ReadyQueue
    ├── FCFS
    └── Priority
        │
        ▼
      Worker
        │
        ├── หยิบ Job
        ├── ทำ workMs
        ├── acquire Semaphore
        ├── ใช้ Resource
        │      └── sleep(resourceMs)
        ├── release Semaphore
        └── บันทึก Statistics
               │
               ▼
            Monitor
        แสดงสถานะระบบ
```

### อธิบายวิธีปิดระบบของกลุ่ม

**Answer:**  
เรียก `interrupt()` กับ Worker ทุกตัวเพื่อขอให้หยุด จากนั้นใช้ `join()` รอ `Monitor`, `JobGenerator`, `Scheduler` และ Worker ทุกตัวให้จบจริงก่อนปิดโปรแกรม

```java
for (Worker worker : workers) {
    worker.interrupt();
}

try {
    monitor.join();
    jobGenerator.join();
    scheduler.join();

    for (Worker worker : workers) {
        worker.join();
    }
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    return;
}
```

### ถ้าเพิ่ม Worker ระหว่างทำงาน วิธี shutdown นี้ยังใช้ได้หรือไม่?

**Answer:**  
ยังใช้ได้ ถ้า Worker ที่เพิ่มถูกเก็บไว้ใน `workers` list ก่อนเข้าสู่ขั้นตอน shutdown เพราะ loop จะ `interrupt()` และ `join()` Worker ตัวนั้นด้วย

### `interrupt()` และ `join()` คืออะไร?

**Answer:**

```text
interrupt() → ขอให้ Thread หยุด
join()      → รอจน Thread นั้นจบจริง
```

`interrupt()` ไม่ได้ฆ่า Thread ทันที แต่เป็นการส่งสัญญาณให้ Thread จัดการการหยุดตัวเอง ส่วน `join()` ทำให้ Thread ปัจจุบันรอจน Thread เป้าหมายจบ