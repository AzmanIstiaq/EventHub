# TEST REPORT

## Commit Info
- **Latest Commit Hash:** f5e34f8  
- **Date:** 12 Oct 2025  
- **Branch:** main 

---

## 1. Latest Test Run (12 Oct 2025 @ 22:14)
| Metric        | Count |
|---------------|-------|
| **Total**     | 132   |
| **Passed**    | 132   |
| **Failed**    | 0     |
| **Ignored**   | 0     |
| **Duration**  | 9.54 s |

> IntelliJ IDEA 2023.2.1 – JUnit-5 – JDK-17  
> Coverage (IntelliJ built-in): **82 % line, 76 % method, 100 % class**

---

## 2. Historical Run (21 Sep 2025) – kept for comparison
| Metric        | Count |
|---------------|-------|
| **Total**     | 60    |
| **Passed**    | 60    |
| **Failed**    | 0     |
| **Duration**  | 7.54  |

---

## 3. What’s New Since Last Report
* 72 extra tests added (132 vs 60) – mainly **BanService**, **AdminEventController**, **EventGallery**, **AuditLog** suites.  
* **0 regressions** – all old tests still green.  
* Coverage jumped from **76 % → 82 % method**, **82 % → 100 % class**.  
* First **full-green build** on Sprint-2 

---

## 4. How to Run the Tests

### 4.1 Local (command line)
```bash
# entire suite
./mvnw clean test

# with JaCoCo coverage report
./mvnw clean test jacoco:report
# open target/site/jacoco/index.html
```

### 4.2 IntelliJ IDEA
> Right-click `src/test/java` → Run 'All Tests'  
> Enable “Track Coverage” before running.  
> Export HTML snapshot via *Run tool-window → Export Coverage Report*.

---

## 5. Traceability Quick-View

| User Story                    | Key New Tests                         | Status     |
| ----------------------------- | ------------------------------------- | -----------|
| **Ban / Un-ban users**        | `BanServiceTest` (17 cases)           | 100 % pass |
| **Admin hide/un-hide events** | `AdminEventControllerTest` (15 cases) | 100 % pass |
| **Event gallery upload**      | `EventGalleryServiceTest` (4 cases)   | 100 % pass |
| **Audit logging**             | `AuditLogServiceTest` (1 case)        | 100 % pass |
| **Old RSVP & create flows**   | `EventControllerTest` (24 cases)      | 100 % pass |

---

## 6. Known Issues 
> Coverage still below 85 % – non-critical getters/setters uncovered.

---

## 7. Conclusion
> Sprint-2 exit criteria met – all PBI tests green, no regressions, coverage above client minimum (70 %).  
> Ready for Milestone 3 enhancements & monitoring phase.
