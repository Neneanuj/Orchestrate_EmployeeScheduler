USE [master]
GO
/****** Object:  Database [SchedulingSystem]    Script Date: 25-11-2025 00:31:02 ******/
CREATE DATABASE [SchedulingSystem]
 CONTAINMENT = NONE
 ON  PRIMARY 
( NAME = N'SchedulingSystem', FILENAME = N'E:\SSMS\MSSQL17.MSSQLSERVER\MSSQL\DATA\SchedulingSystem.mdf' , SIZE = 8192KB , MAXSIZE = UNLIMITED, FILEGROWTH = 65536KB )
 LOG ON 
( NAME = N'SchedulingSystem_log', FILENAME = N'E:\SSMS\MSSQL17.MSSQLSERVER\MSSQL\DATA\SchedulingSystem_log.ldf' , SIZE = 8192KB , MAXSIZE = 2048GB , FILEGROWTH = 65536KB )
 WITH CATALOG_COLLATION = DATABASE_DEFAULT, LEDGER = OFF
GO
ALTER DATABASE [SchedulingSystem] SET COMPATIBILITY_LEVEL = 170
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [SchedulingSystem].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [SchedulingSystem] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [SchedulingSystem] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [SchedulingSystem] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [SchedulingSystem] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [SchedulingSystem] SET ARITHABORT OFF 
GO
ALTER DATABASE [SchedulingSystem] SET AUTO_CLOSE OFF 
GO
ALTER DATABASE [SchedulingSystem] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [SchedulingSystem] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [SchedulingSystem] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [SchedulingSystem] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [SchedulingSystem] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [SchedulingSystem] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [SchedulingSystem] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [SchedulingSystem] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [SchedulingSystem] SET  ENABLE_BROKER 
GO
ALTER DATABASE [SchedulingSystem] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [SchedulingSystem] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE [SchedulingSystem] SET TRUSTWORTHY OFF 
GO
ALTER DATABASE [SchedulingSystem] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [SchedulingSystem] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [SchedulingSystem] SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE [SchedulingSystem] SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE [SchedulingSystem] SET RECOVERY FULL 
GO
ALTER DATABASE [SchedulingSystem] SET  MULTI_USER 
GO
ALTER DATABASE [SchedulingSystem] SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE [SchedulingSystem] SET DB_CHAINING OFF 
GO
ALTER DATABASE [SchedulingSystem] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [SchedulingSystem] SET TARGET_RECOVERY_TIME = 60 SECONDS 
GO
ALTER DATABASE [SchedulingSystem] SET DELAYED_DURABILITY = DISABLED 
GO
ALTER DATABASE [SchedulingSystem] SET ACCELERATED_DATABASE_RECOVERY = OFF  
GO
ALTER DATABASE [SchedulingSystem] SET OPTIMIZED_LOCKING = OFF 
GO
EXEC sys.sp_db_vardecimal_storage_format N'SchedulingSystem', N'ON'
GO
ALTER DATABASE [SchedulingSystem] SET QUERY_STORE = ON
GO
ALTER DATABASE [SchedulingSystem] SET QUERY_STORE (OPERATION_MODE = READ_WRITE, CLEANUP_POLICY = (STALE_QUERY_THRESHOLD_DAYS = 30), DATA_FLUSH_INTERVAL_SECONDS = 900, INTERVAL_LENGTH_MINUTES = 60, MAX_STORAGE_SIZE_MB = 1000, QUERY_CAPTURE_MODE = AUTO, SIZE_BASED_CLEANUP_MODE = AUTO, MAX_PLANS_PER_QUERY = 200, WAIT_STATS_CAPTURE_MODE = ON)
GO
USE [SchedulingSystem]
GO
/****** Object:  User [nene]    Script Date: 25-11-2025 00:31:03 ******/
CREATE USER [nene] FOR LOGIN [nene] WITH DEFAULT_SCHEMA=[dbo]
GO
ALTER ROLE [db_owner] ADD MEMBER [nene]
GO
/****** Object:  Table [dbo].[employee_expertise]    Script Date: 25-11-2025 00:31:03 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[employee_expertise](
	[expertise_id] [int] IDENTITY(1,1) NOT NULL,
	[employee_id] [int] NOT NULL,
	[sport_id] [int] NOT NULL,
	[expertise_level] [varchar](20) NULL,
PRIMARY KEY CLUSTERED 
(
	[expertise_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[employee_id] ASC,
	[sport_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[employees]    Script Date: 25-11-2025 00:31:03 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[employees](
	[employee_id] [int] IDENTITY(1,1) NOT NULL,
	[user_id] [int] NOT NULL,
	[first_name] [varchar](50) NOT NULL,
	[last_name] [varchar](50) NOT NULL,
	[max_hours_per_week] [int] NULL,
	[is_supervisor_eligible] [bit] NULL,
	[active_status] [bit] NULL,
PRIMARY KEY CLUSTERED 
(
	[employee_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[game_schedules]    Script Date: 25-11-2025 00:31:03 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[game_schedules](
	[schedule_id] [int] IDENTITY(1,1) NOT NULL,
	[sport_id] [int] NOT NULL,
	[game_date] [date] NOT NULL,
	[start_time] [time](7) NOT NULL,
	[end_time] [time](7) NOT NULL,
	[location] [varchar](100) NOT NULL,
	[required_supervisors] [int] NULL,
	[required_referees] [int] NOT NULL,
	[created_by] [int] NOT NULL,
	[created_at] [datetime] NULL,
	[schedule_cycle_start] [date] NOT NULL,
	[schedule_cycle_end] [date] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[schedule_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[permanent_conflicts]    Script Date: 25-11-2025 00:31:03 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[permanent_conflicts](
	[conflict_id] [int] IDENTITY(1,1) NOT NULL,
	[employee_id] [int] NOT NULL,
	[day_of_week] [varchar](20) NOT NULL,
	[start_time] [time](7) NOT NULL,
	[end_time] [time](7) NOT NULL,
	[reason] [varchar](200) NULL,
PRIMARY KEY CLUSTERED 
(
	[conflict_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[seasonal_availability]    Script Date: 25-11-2025 00:31:03 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[seasonal_availability](
	[availability_id] [int] IDENTITY(1,1) NOT NULL,
	[employee_id] [int] NOT NULL,
	[season] [varchar](20) NOT NULL,
	[year] [int] NOT NULL,
	[day_of_week] [varchar](20) NOT NULL,
	[start_time] [time](7) NOT NULL,
	[end_time] [time](7) NOT NULL,
	[is_preferred] [bit] NULL,
PRIMARY KEY CLUSTERED 
(
	[availability_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[shifts]    Script Date: 25-11-2025 00:31:03 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[shifts](
	[shift_id] [int] IDENTITY(1,1) NOT NULL,
	[game_schedule_id] [int] NOT NULL,
	[position_type] [varchar](20) NOT NULL,
	[position_number] [int] NOT NULL,
	[assigned_employee_id] [int] NULL,
	[recommendation_a_id] [int] NULL,
	[recommendation_b_id] [int] NULL,
	[assignment_status] [varchar](20) NULL,
	[assigned_at] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[shift_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[game_schedule_id] ASC,
	[position_type] ASC,
	[position_number] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[sports]    Script Date: 25-11-2025 00:31:03 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[sports](
	[sport_id] [int] IDENTITY(1,1) NOT NULL,
	[sport_name] [varchar](50) NOT NULL,
	[default_duration_minutes] [int] NOT NULL,
	[required_supervisors] [int] NULL,
	[required_referees] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[sport_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[sport_name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[time_off_requests]    Script Date: 25-11-2025 00:31:03 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[time_off_requests](
	[request_id] [int] IDENTITY(1,1) NOT NULL,
	[employee_id] [int] NOT NULL,
	[start_date] [date] NOT NULL,
	[end_date] [date] NOT NULL,
	[request_status] [varchar](20) NOT NULL,
	[reason] [varchar](500) NULL,
	[submitted_at] [datetime] NULL,
	[reviewed_by] [int] NULL,
	[reviewed_at] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[request_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[users]    Script Date: 25-11-2025 00:31:03 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[users](
	[user_id] [int] IDENTITY(1,1) NOT NULL,
	[username] [varchar](50) NOT NULL,
	[password_hash] [varchar](255) NOT NULL,
	[role] [varchar](20) NOT NULL,
	[created_at] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[weekly_hours]    Script Date: 25-11-2025 00:31:03 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[weekly_hours](
	[tracking_id] [int] IDENTITY(1,1) NOT NULL,
	[employee_id] [int] NOT NULL,
	[week_start_date] [date] NOT NULL,
	[total_scheduled_hours] [decimal](4, 2) NULL,
	[last_updated] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[tracking_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[employee_id] ASC,
	[week_start_date] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
ALTER TABLE [dbo].[employee_expertise] ADD  DEFAULT ('INTERMEDIATE') FOR [expertise_level]
GO
ALTER TABLE [dbo].[employees] ADD  DEFAULT ((20)) FOR [max_hours_per_week]
GO
ALTER TABLE [dbo].[employees] ADD  DEFAULT ((0)) FOR [is_supervisor_eligible]
GO
ALTER TABLE [dbo].[employees] ADD  DEFAULT ((1)) FOR [active_status]
GO
ALTER TABLE [dbo].[game_schedules] ADD  DEFAULT ((1)) FOR [required_supervisors]
GO
ALTER TABLE [dbo].[game_schedules] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[game_schedules] ADD  DEFAULT (getdate()) FOR [schedule_cycle_start]
GO
ALTER TABLE [dbo].[game_schedules] ADD  DEFAULT (dateadd(day,(7),getdate())) FOR [schedule_cycle_end]
GO
ALTER TABLE [dbo].[seasonal_availability] ADD  DEFAULT ((0)) FOR [is_preferred]
GO
ALTER TABLE [dbo].[shifts] ADD  DEFAULT ('UNASSIGNED') FOR [assignment_status]
GO
ALTER TABLE [dbo].[sports] ADD  DEFAULT ((1)) FOR [required_supervisors]
GO
ALTER TABLE [dbo].[time_off_requests] ADD  DEFAULT ('PENDING') FOR [request_status]
GO
ALTER TABLE [dbo].[time_off_requests] ADD  DEFAULT (getdate()) FOR [submitted_at]
GO
ALTER TABLE [dbo].[users] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[weekly_hours] ADD  DEFAULT ((0.00)) FOR [total_scheduled_hours]
GO
ALTER TABLE [dbo].[weekly_hours] ADD  DEFAULT (getdate()) FOR [last_updated]
GO
ALTER TABLE [dbo].[employee_expertise]  WITH CHECK ADD FOREIGN KEY([employee_id])
REFERENCES [dbo].[employees] ([employee_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[employee_expertise]  WITH CHECK ADD FOREIGN KEY([sport_id])
REFERENCES [dbo].[sports] ([sport_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[employees]  WITH CHECK ADD FOREIGN KEY([user_id])
REFERENCES [dbo].[users] ([user_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[game_schedules]  WITH CHECK ADD FOREIGN KEY([created_by])
REFERENCES [dbo].[users] ([user_id])
GO
ALTER TABLE [dbo].[game_schedules]  WITH CHECK ADD FOREIGN KEY([sport_id])
REFERENCES [dbo].[sports] ([sport_id])
GO
ALTER TABLE [dbo].[permanent_conflicts]  WITH CHECK ADD FOREIGN KEY([employee_id])
REFERENCES [dbo].[employees] ([employee_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[seasonal_availability]  WITH CHECK ADD FOREIGN KEY([employee_id])
REFERENCES [dbo].[employees] ([employee_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[shifts]  WITH CHECK ADD FOREIGN KEY([assigned_employee_id])
REFERENCES [dbo].[employees] ([employee_id])
GO
ALTER TABLE [dbo].[shifts]  WITH CHECK ADD FOREIGN KEY([game_schedule_id])
REFERENCES [dbo].[game_schedules] ([schedule_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[shifts]  WITH CHECK ADD FOREIGN KEY([recommendation_a_id])
REFERENCES [dbo].[employees] ([employee_id])
GO
ALTER TABLE [dbo].[shifts]  WITH CHECK ADD FOREIGN KEY([recommendation_b_id])
REFERENCES [dbo].[employees] ([employee_id])
GO
ALTER TABLE [dbo].[time_off_requests]  WITH CHECK ADD FOREIGN KEY([employee_id])
REFERENCES [dbo].[employees] ([employee_id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[time_off_requests]  WITH CHECK ADD FOREIGN KEY([reviewed_by])
REFERENCES [dbo].[users] ([user_id])
GO
ALTER TABLE [dbo].[weekly_hours]  WITH CHECK ADD FOREIGN KEY([employee_id])
REFERENCES [dbo].[employees] ([employee_id])
GO
USE [master]
GO
ALTER DATABASE [SchedulingSystem] SET  READ_WRITE 
GO
