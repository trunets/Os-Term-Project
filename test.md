เปลี่ยนจำนวน Worker หรือจำนวนสิทธิ์ของ Semaphore แล้วอธิบายว่าคาดว่าระบบจะเปลี่ยนแปลงอย่างไร
Answer: เพิ่ม Worker หรือ Semaphore → งานรอคิวน้อยลงและทำงานเร็วขึ้น; ลด → งานรอมากขึ้นและช้าลง.

ชี้ข้อมูลร่วมที่หลาย Thread แก้ไขได้ เช่น runningJobs และอธิบายว่าถ้าเอาการป้องกันออกจะเกิดปัญหาอะไร
Answer: runningJobs และ Statistics เป็นข้อมูลที่หลาย Thread แก้ไขร่วมกัน หากไม่มีการป้องกัน อาจเกิด Race Condition ทำให้ค่าผิดพลาดหรือข้อมูลสูญหาย.

อธิบาย FCFS เรียงตามลำดับเข้าคิว, Priority เรียง Priority น้อยก่อน และถ้าเท่ากันใช้ SequenceNumber เป็น tie-break.

อธิบายวิธีปิดระบบของกลุ่ม และตอบได้ว่าถ้าเพิ่ม Worker ระหว่างทำงาน วิธีนั้นยังใช้ได้ถูกต้องหรือไม่
Answer: ปิดระบบโดยรอให้ Job ทำงานครบ แล้วหยุด Worker และ Monitor จากนั้น join() ทุก Thread ให้จบก่อนออกจากโปรแกรม. วิธีนี้ไม่รองรับการเพิ่ม Worker ระหว่างทำงาน เพราะจำนวน Worker ถูกกำหนดตอนเริ่มระบบ.

อธิบายว่าถ้า Worker ถูก interrupt หลัง acquire resource จะป้องกันไม่ให้ permit หายได้อย่างไร
Answer: 

เปรียบเทียบ Thread.State ตอนรอ Semaphore กับตอน Thread.sleep(resourceMs)
Answer: 

ใช้สมการ TAT = WT + workMs + Resource Wait + resourceMs และชี้ว่าถ้าค่าไม่ตรงกับ timestamp ใด น่าจะเกิดข้อผิดพลาดที่จุดใด
Answer: 

อธิบายว่า Generative AI ช่วยสร้างส่วนใด และกลุ่มตรวจสอบความถูกต้องของส่วนนั้นอย่างไร
Answer: 

อธิบายได้ว่า Thread แต่ละตัวทำอะไร 
Answer: 

ข้อมูลใดใช้ร่วมกัน 
Answer: 

ป้องกันการชนกันอย่างไร 
Answer: 

ใช้ Semaphore/Scheduling อย่างไร 
Answer: 

ค่าที่วัดหมายถึงอะไร 
Answer: 

และปิดระบบอย่างไร 
Answer: 

คะแนน Demo จะสะท้อนความเข้าใจของแต่ละคน