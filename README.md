# VeriDex Backend

The Operating System for Sustainability-Linked Loans (SLL)  
**VeriDex** is an automated verification engine addressing the **"Capture, Verify, and Share"** problem in the $860B **Sustainability-Linked Loan market.** This backend powers **multi-party workflows** between **Agents, Borrowers, and Syndicate Lenders.**

---

## Key Capabilities

### 1. AI-Powered Risk Assessment (Ingestion Layer)
**Problem:** Credit Officers currently spend days manually reading unstructured ESG PDFs.  
**Solution:** With the integration of LLMs:  

- Raw Borrower PDFs are ingested.
- Industry categorization is automated.
- **Risk Scores (0–100)** and **Margin Recommendations (in Basis Points)** are calculated dynamically.

---

### 2. Dynamic Margin Ratchet Engine
Sustainability-linked loans adjust interest rates based on borrower KPI (Key Performance Indicator) performance.  

**Implementation Logic:**
- **Met Target:** Reduce Margin (e.g., -0.25%).
- **Missed Target:** Increase Margin (e.g., +0.05%).  

This **eliminates Excel-based errors** through automated calculations.

---

### 3. Automated Verification Layer
**How It Works:**  
- External "Truth Sources" (e.g., Utility APIs, Carbon Registries) are mocked for demo.
- Verification Service ensures cross-checks of **Borrower KPIs.**

**Constraint:** The financial margin calculation endpoint unlocks **only** when verification **status = VERIFIED** (variance ≤ 5%).

---

### 4. Immutable Audit Trail
All key business actions—**Risk Assessment, Loan Creation, KPI Verification, Margin Changes—are cryptographically hashed** and stored for compliance.  

**Ready for Blockchain:** Hashing is compatible with permissioned ledgers like **Hyperledger** or **Corda.**

---

## 🛠 Tech Stack

- **Core:** Java 17, Spring Boot 3.2  
- **AI Integration:** Gemini API
- **Security:** Role-Based Access Control (RBAC)  
- **Database:**PostgreSQL  
- **Build Tool:** Maven  

---

##  Architecture & Data Model
**Pattern:** Controller ⇢ Service ⇢ Repository  

- **Loan Entity:** Aggregate root managing the deal.  
- **KPI Entity:** Tracks financial targets (Baseline, Target) and performance implications (Margin Adjustments).  
- **SyndicateMembers:** Ensures lender transparency.

---

## How to Run

### Prerequisites
- JDK 17 or higher  
- Maven  
- GEMINI API Key  

---

### Steps

1. **Clone the Repo**  
   ```bash
   git clone https://github.com/Azam0221/Veridex_Backend.git
   cd Veridex_Backend
   ```
2. **Configure Environment**  
   Update `application.properties` with your OpenAI Key:  
   ```
   spring.ai.openai.api-key=YOUR_KEY_HERE
   ```
3. **Build and Run**  
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
---

## Roadmap (Scaling Strategy)
**Phase 1 (Current):** Proof of Concept with simulated APIs.  
**Phase 2:** Real-time ESG Integrations (e.g., MSCI, Sustainalytics).  
**Phase 3:** Migration to Blockchain for **Multi-Bank Audit Transparency.**

---

## Built for **LMA Edge Hackathon 2026**  
**Team:** Global Hackers Connect  
