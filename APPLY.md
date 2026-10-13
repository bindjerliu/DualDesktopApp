# OMEL — how to apply

1. Copy everything in this folder over your project folder (the one that contains `pom.xml`); overwrite when asked.
2. Remove the build output and the real order data from git:
   ```
   git rm -r --cached target
   git rm --cached data/orders.txt
   ```
   (`.gitignore` now keeps them out; `data/orders.sample.txt` replaces the data file.)
3. `mvn clean test` then `mvn javafx:run` and click through both screens once.
4. Add 2-3 screenshots to the README (customer screen, admin screen).

## What changed
- `OrderDatabase` — updating one order no longer deletes lines the app can't parse; atomic rewrite; one lock for all instances; ids/timestamps created in one step.
- `Order` / `MenuItem` / `CartItem` — money is `BigDecimal`; totals are written locale-independently (before, a decimal-comma computer wrote "5,70" and the order could not be read back).
- `MenuService` — item availability is remembered between runs.
- `AuthService` + `AdminLoginController` — credentials from environment variables (demo default flagged); the error message no longer prints the password.
- `OrderStats` — "Revenue" now counts completed orders only.
- `Validation` — real email/name/notes checks.
- FXML — "Revenue (completed)"; login subtitle no longer claims "Secure".
- `pom.xml` — JUnit 5 + surefire; `.github/workflows/ci.yml` runs `mvn test`.
