-- 1️⃣ สร้าง database campusjobs (ถ้ายังไม่มี)
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'campusjobs')
BEGIN
    CREATE DATABASE campusjobs COLLATE Thai_CI_AS;
END
GO

-- 2️⃣ ใช้ database campusjobs
USE campusjobs;
GO

-- 3️⃣ สร้าง table [user] (8 columns) กับ table [noti]
IF OBJECT_ID('dbo.[users]', 'U') IS NOT NULL
BEGIN
    DROP TABLE dbo.[users];
    DROP TABLE dbo.[notifications]
END
GO

IF OBJECT_ID('dbo.[notifications]', 'U') IS NOT NULL
BEGIN
    DROP TABLE dbo.[notifications];
END
GO

CREATE TABLE dbo.[users] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [department] NVARCHAR(255) NOT NULL,
    [display_name_en] NVARCHAR(255) NOT NULL,
    [display_name_th] NVARCHAR(255) NOT NULL,
    [email] NVARCHAR(255) NOT NULL,
    [faculty] NVARCHAR(255) NOT NULL,
    [role] NVARCHAR(50) NOT NULL,
    [username] NVARCHAR(100) NOT NULL,
    [password] NVARCHAR(255) NULL
)
CREATE TABLE dbo.[notifications] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [user_id] BIGINT NOT NULL,
    [description] NVARCHAR(500) NULL,
    [is_read] BIT NOT NULL DEFAULT 0,
    [created_at] DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    [link_url] NVARCHAR(500) NULL,
    CONSTRAINT FK_notifications_users FOREIGN KEY([user_id]) REFERENCES dbo.[users]([id])
);
GO

-- 4️⃣ Insert record admin
INSERT INTO dbo.[users] (
    [department],
    [display_name_en],
    [display_name_th],
    [email],
    [faculty],
    [role],
    [username],
    [password]
)
VALUES (
    N'ภาควิชาวิทยาการคอมพิวเตอร์',
    N'admin',
    N'แอดมิน',
    N'admin@dome.tu.ac.th',
    N'คณะวิทยาศาสตร์และเทคโนโลยี',
    N'ROLE_TEACHER',
    N'admin',
    N'{bcrypt}$2a$10$DOGhn707grtyUsno1lSrCuu43DRIbRFf1sAFfdUURGbJ42Co7Xasu'  -- รหัสผ่าน: admin (bcrypt hashed)
);
GO