# TEST REPORT

## Commit Info
- **Commit Hash:** c33d676  
- **Date:** 21/09/2025

## Test Results Summary 
*(Last Run ~1:40pm)*

- **Total Tests:** 60  
- **Passed:** 60  
- **Failed:** 0  
- **Skipped:** 0

> Evidence: IntelliJ run “webapp in webapp” — **60 of 60 tests passed**. Coverage panel shown below was captured from the same run.

---

## Code Coverage (IntelliJ)
**Overall (au.edu.rmit.sept.webapp)**  
- **Class:** **100%** (25/25)  
- **Method:** **76%** (128/168)  
- **Line:** **82%** (400/484)

**By package (high-level)**

| Package | Class % | Method % | Line % |
|---|---:|---:|---:|
| `config` | 100% (1/1) | — | **82%** |
| `controller` | **100% (7/7)** | **75% (24/32)** | **82% (173/210)** |
| `model` | 100% (7/7) | 76% | 82% |
| `repository` | 100% (0/0) | 100% (0/0) | 100% (0/0) |
| `security` | 100% (2/2) | **91% (11/12)** | **93%** |
| `service` | 100% (7/7) | 68% (22/32) | 78% (48/61) |
| `WebappApplication` | 100% (1/1) | 66% (2/3) | 97% (37/38) |

> **Overall method coverage: 76%**

---

## Unit Tests
- **EventServiceTest** – save/find/upcoming/past events  
- **CategoryServiceTest, KeywordServiceTest, OrganiserServiceTest, UserServiceTest** – CRUD and retrieval  
- **RegistrationServiceTest** – register, prevent duplicate  
- **FeedbackServiceTest** – save + retrieve feedback  

---

## Controller / User Story Tests
- **EventCreateUserStoryTest** – organiser creates events  
- **EventControllerWebMvcTest** – organiser endpoints  
- **PublicEventControllerTest** – student browse/search/register/cancel  
- **RsvpUserStoryTest** – RSVP flow (register, cancel, my events, not logged in)  
- **AdminControllerTest** – basic admin endpoints  
- **AdminUserStoryTest** – admin event list + detail  
- **AdminControllerExtendedTest** – admin extended scenarios  
- **UserControllerTest** – login/register endpoints  
- **FeedbackControllerTest** – feedback submission  

---

## Acceptance Tests
- **AcceptanceScenariosTest** (Gherkin-style)  
  - Create event  
  - Prevent past event  
  - RSVP to event  
  - Prevent duplicate RSVP  

---

## Boundary & Negative Cases Covered
- **Prevent creating past events** (validation) — *AcceptanceScenariosTest, EventCreateUserStoryTest*  
- **Duplicate RSVP blocked** — *RegistrationServiceTest, RsvpUserStoryTest*  
- **Invalid/unknown IDs handled gracefully** (e.g., delete redirects) — *AdminControllerExtendedTest*  
- **User lookup failure throws `UsernameNotFoundException`** — *CustomUserDetailsServiceTest*  
- **Repository returns `Optional.empty()` when missing** — *EventServiceTest*  
- **Public vs. authenticated access boundaries** — *PublicEventControllerTest*  
- **Feedback duplicate prevention / error paths** — *FeedbackServiceTest*  

---

## Traceability Matrix

| User Story | Acceptance Tests | Unit Tests | Controller Tests |
|------------|------------------|------------|------------------|
| **US-01 Organiser creates events** | EventCreateUserStoryTest, AcceptanceScenariosTest | EventServiceTest | EventControllerWebMvcTest |
| **US-02 Student browses & RSVPs** | RsvpUserStoryTest, PublicEventControllerTest, AcceptanceScenariosTest | RegistrationServiceTest | **PublicEventControllerTest** |
| **US-03 Admin manages system** | AdminUserStoryTest, AdminControllerExtendedTest | – | AdminControllerTest |
| **US-04 Feedback** | – | FeedbackServiceTest | FeedbackControllerTest |
| **US-05 User login/registration** | – | UserServiceTest | UserControllerTest |
