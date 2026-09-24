# TODO


- [x] ReadyQueue.java

- [] JobGenerator.java — แนะนำให้ทำต่อทันที
ทำให้ Job ถูกปล่อยตาม arrivalMs และส่งต่อไปยัง Scheduler

- [] Scheduler.java
รับ Job จาก Generator แล้วใส่เข้า ReadyQueue

- [] Worker.java
เริ่มจาก Worker 1 ตัวก่อน ให้ flow Generator → Scheduler → ReadyQueue → Worker ทำงานครบ
เพิ่ม Worker หลายตัวใน Main.java

- [] ResourceManager.java
ค่อยเพิ่ม PRINTER / DATABASE และ Semaphore

- [] Job.java + Statistics.java
เพิ่ม metrics เช่น Waiting, Turnaround, Resource Wait, Throughput

- [] Monitor.java

- [] Main.java — shutdown/graceful termination
เก็บไว้ท้ายสุด เพราะเป็นส่วนที่ซับซ้อนที่สุด