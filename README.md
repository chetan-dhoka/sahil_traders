# Sahil Traders – Billing System

A simple, clean billing system for **Sahil Traders** (Grain & Grocery Broker, Pune).
Generates PDF bills that match the original hand-written Sahil Traders format exactly.

---

## How to Run (Laptop – easiest way)

### Option A: Run directly with Java + MySQL

**Requirements:** Java 8+, MySQL 8, Maven

1. Create the database:
```sql
CREATE DATABASE sahil_billing;
```

2. Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sahil_billing?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

3. Run:
```bash
mvn spring-boot:run
```

4. Open browser: **http://localhost:8080**

---

### Option B: Docker (one command)

**Requirements:** Docker Desktop installed

```bash
docker compose up -d
```

Open: **http://localhost:8080**

---

## How to Use

### Creating a Bill
1. Click **New Bill**
2. Enter the **Bill Date** (when you are issuing the bill)
3. Type the **Seller name** (the `Name` field on the physical bill) — autocomplete will suggest known sellers
4. Fill each row:
   - **Date** → date goods were sold to that buyer
   - **Particulars (Buyer)** → buyer's name — autocomplete works here too
   - **Type** → commodity (Wheat, Rice, Poha etc)
   - **Qty** → number of bags
   - **Rate** → your brokerage rate
   - **Amount** → calculated automatically ✅
5. Click **+ Add Row** for more transactions
6. Click **Save Bill**

### Downloading PDF
- Open any bill → click **Download PDF**
- PDF looks exactly like your physical Sahil Traders bill

### Reports
- Go to **Reports**
- Select period: Full Year / Half Year / Quarterly / Custom dates
- Optionally filter by a specific **Seller**
- Click **PDF** or **CSV** to download

### Editing Firm Details
- Go to **Settings**
- Edit firm name, address, mobile, PAN, footer text
- These appear on every PDF

---

## Bill Format (matches physical bill)

```
┌─────────────────────────────────────────────────┐
│  Subject to Jurisdiction  ॥ महावीराय नमः ॥  Mob │
├──────────────────────────────────────────────────┤
│  [ST]  Sahil Traders                             │
│        Grain & Grocery Broker                    │
│        Anand Heights, Kondhwa, Pune 48.          │
│  No: 184                       Date: 30-04-2024  │
│  Name: Ravi Kumar                                │
├────────┬────────────┬────────┬──────┬──────┬─────┤
│  Date  │ Particulars│  Type  │ Qty. │ Rate │ Amt │
├────────┼────────────┼────────┼──────┼──────┼─────┤
│        │            │        │      │      │     │
│ (14 rows minimum, just like the physical bill)   │
├────────┴────────────┴────────┴──────┴──────┴─────┤
│                              Total:  1875.00      │
├──────────────────────────────────────────────────┤
│ PAN: COHPC2282R  ◇ चूकभूल देणे घेणे  For Sahil │
└──────────────────────────────────────────────────┘
```

---

## Project Structure

```
sahil-traders/
├── src/main/java/com/sahil/billing/
│   ├── BillingApplication.java
│   ├── config/WebConfig.java
│   ├── controller/
│   │   ├── HomeController.java        ← Home + Settings
│   │   ├── BillController.java        ← Create/Edit/View/PDF
│   │   ├── ReportController.java      ← Reports + PDF + CSV
│   │   └── SuggestController.java     ← Autocomplete API
│   ├── dto/
│   │   ├── BillForm.java
│   │   └── BillItemForm.java
│   ├── entity/
│   │   ├── Bill.java
│   │   ├── BillItem.java
│   │   ├── Seller.java
│   │   ├── Buyer.java
│   │   ├── FirmSettings.java
│   │   └── BillSequence.java
│   ├── repository/  (Spring Data JPA)
│   ├── service/BillService.java
│   ├── pdf/PdfService.java
│   └── util/DateRangeUtil.java
├── src/main/resources/
│   ├── templates/
│   │   ├── home.html                  ← Dashboard
│   │   ├── bill-form.html             ← New / Edit bill
│   │   ├── bill-view.html             ← View bill
│   │   ├── bill-list.html             ← Search bills
│   │   ├── reports.html               ← Reports
│   │   ├── settings.html              ← Firm settings
│   │   └── pdf/bill.html              ← PDF template
│   ├── static/js/bill-form.js         ← Autocomplete + calc
│   ├── db/migration/V1__init.sql      ← Schema + seed data
│   └── application.properties
├── pom.xml
├── Dockerfile
└── docker-compose.yml
```

---

## Technologies Used

| What          | Technology                        |
|---------------|-----------------------------------|
| Backend       | Java 8, Spring Boot 2.7           |
| Web UI        | Thymeleaf + Bootstrap 5           |
| Database      | MySQL 8 + Flyway migrations       |
| ORM           | Spring Data JPA + Hibernate       |
| PDF           | OpenHTMLtoPDF                     |
| CSV           | OpenCSV                           |
| Build         | Maven                             |
| Container     | Docker + Docker Compose           |

---

## Multi-firm (Sahil Traders + S S Dhoka)

* On every start the app shows **Select Firm**. After you pick a firm, bills, reports,
  PDFs and settings all belong to that firm until you click **Switch Firm**.
* Each firm has its own bill numbers (both start at **1**), its own bills and reports,
  and its own name, PAN, address, phone, footer, logo and colour.
* **Sellers and buyers are shared** between the firms (a typo is fixed once).
* The first start after this update runs `V2__multi_firm.sql`, which **deletes the
  existing test bills** and restarts numbering at 1 for both firms.
* Colour themes: Maroon (Sahil Traders), Navy Blue (S S Dhoka), Plum. Change a firm's
  theme or initials in **Settings**. The S S Dhoka logo is `static/logos/ssd-logo.png`;
  other designs are in `extras/logo-options/`.
* `extras/optional_clear_sellers_and_buyers.sql` empties the shared name lists (optional).

Safety: a form opened for one firm cannot be saved into another firm; if you switch firm
and press Save on an old tab, nothing is saved and you are told to reopen the form.
