#!/bin/bash

# (ตัวแปร $SA_PASSWORD จะถูกส่งมาจาก docker-compose)

echo "Waiting for SQL Server (db) to be ready..."
DB_STATUS=1

# --- นี่คือ "Loop" ที่เราคุยกัน ---
# (จะวนเช็กไปเรื่อยๆ จนกว่าจะเชื่อมต่อสำเร็จ)
until [ $DB_STATUS -eq 0 ]; do
    /opt/mssql-tools/bin/sqlcmd -S db -U sa -P "$SA_PASSWORD" -l 1 -Q "SELECT 1" > /dev/null 2>&1
    DB_STATUS=$? # (เช็กผลลัพธ์: 0 = สำเร็จ)
    
    if [ $DB_STATUS -ne 0 ]; then
        echo "SQL Server not ready. Retrying in 5 seconds..."
        sleep 5
    fi
done

# (ถ้ามาถึงตรงนี้ได้ แปลว่า DB พร้อม 100%)
echo "SQL Server is ready! Initializing database..."

# --- รันสคริปต์ init.sql ---
/opt/mssql-tools/bin/sqlcmd -S db -U sa -P "$SA_PASSWORD" -i /scripts/init.sql

INIT_STATUS=$? # (เช็กว่ารันสำเร็จไหม)

# --- เช็กผลลัพธ์สุดท้าย (แก้ปัญหา "โกหก") ---
if [ $INIT_STATUS -eq 0 ]; then
    echo "Database initialized successfully!"
    exit 0 # (จบแบบสำเร็จ)
else
    echo "Database initialization FAILED!"
    exit 1 # (จบแบบล้มเหลว -> Container นี้จะพัง)
fi