# Relief-OP Setup & Testing Guide

## ✅ What's Been Fixed & Improved

### 1. **Role Selection Screen**
- App now starts with a 3-role selection screen (Admin, Operator, Coordinator)
- Select a role to proceed to login
- Colors: Admin (Red), Operator (Blue), Coordinator (Green)

### 2. **Conditional Register Tab**
- ✅ **Admin Role**: Only Login tab (no register available)
- ✅ **Operator & Coordinator**: Both Login and Register tabs available
- New accounts created as Operator role by default

### 3. **Enhanced Login UI**
- Improved logo and branding (🚨 Relief-OP)
- Demo credentials panel with all role credentials
- Role indicator in tab name (e.g., "🔐 LOGIN (Admin)")
- Larger window size (600x580)

### 4. **Sample Data Added**
- Created `sql/sample_data.sql` with:
  - 8+ test victims with geo-coordinates
  - 6+ requests with severity levels
  - Resource stock levels (5000L water, 2000 med kits, 3000 canned food)
  - Shelter resource inventory
  - 8+ helpers & volunteers
  - 5+ supplier entries
  - Audit logs for forensic view

### 5. **Database Credential Security**
- Moved hard-coded credentials to `DBConfig.java`
- Supports environment variable override
- Centralized configuration management

---

## 🚀 Quick Start Guide

### Step 1: Load Sample Data into Database
```sql
-- Open MySQL command line or MySQL Workbench
mysql -u root -p reliefops < C:\Users\AKASH\SEM PROJECT\Relief-OP\sql\schema.sql
mysql -u root -p reliefops < C:\Users\AKASH\SEM PROJECT\Relief-OP\sql\sample_data.sql
```

Or manually:
1. Open MySQL Workbench
2. Run `schema.sql` (creates tables & initial users)
3. Run `sample_data.sql` (adds test data)

### Step 2: Compile & Run
```powershell
cd "C:\Users\AKASH\SEM PROJECT\Relief-OP\java"
.\run.bat
```

Or manually:
```powershell
cd src
javac -cp ".;..\lib\mysql-connector-j-9.6.0.jar" *.java panels\*.java
java -cp ".;..\lib\mysql-connector-j-9.6.0.jar" Main
```

### Step 3: Test the Roles

#### **Admin Login**
- Username: `admin`
- Password: `admin123`
- Features: System Reports, Batch Processing, Simulate Cyclone, Map Dashboard
- Note: No Register tab available

#### **Operator Login**
- Username: `operator`
- Password: `op123`
- Features: Victims, Requests, Map Dashboard
- Note: Register tab available for creating new accounts

#### **Coordinator Login**
- Username: `coord`
- Password: `coord123`
- Features: Shelters, Resources, Volunteers, Helpers, Map Dashboard
- Note: Register tab available

---

## 🗺️ Map Dashboard
1. Click "Launch Interactive Control Center 📍" button
2. Browser will open with Leaflet.js map showing:
   - 🏥 Blue markers for shelters
   - 🔴 Red heat zones for victims
   - Real-time density visualization

---

## 📊 Admin Dashboard Data
The admin dashboard now shows:
- **Demand Trends**: Resource consumption patterns
- **Resource Pressure (%)**: Stock pressure index
- **Forensic Audit Feed**: Recent system actions

> Data populates from `view_demand_trends`, `view_current_stock_pressure`, and `Logs` table

---

## 🐛 Troubleshooting

### No Data in Admin Dashboard?
1. Ensure `sample_data.sql` was run
2. Check MySQL connection in `DBConfig.java`
3. Verify tables populated: `SELECT COUNT(*) FROM Victims;`

### Map Not Opening?
1. Ensure browser is set as default
2. Check internet connection (needs map tiles)
3. Verify temp files can be created in system temp directory

### Register Tab Not Showing for Admin?
✅ This is **correct behavior** - Admin role intentionally has register tab hidden

### Database Connection Error?
1. Start MySQL service
2. Verify credentials in `DBConfig.java`
3. Ensure `reliefops` database exists

---

## 📁 File Changes Made

| File | Change |
|------|--------|
| `LoginFrame.java` | Added role selection screen, conditional tabs |
| `DBConfig.java` | ✨ NEW - Centralized credentials management |
| `sample_data.sql` | ✨ NEW - Test data for all entities |

---

## ✨ Ready to Use!
Your Relief-OP application is now fully configured with:
- ✅ Role-based login
- ✅ Conditional UI elements
- ✅ Sample data for testing
- ✅ Secure credential management
- ✅ Fully functional dashboards

**Happy disaster relief testing! 🚨**
