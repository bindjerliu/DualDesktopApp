# OMEL — Campus Café Ordering App (JavaFX)

A JavaFX desktop application for a university cafeteria ordering system, built for the RIT Kosovo
campus café. It has two modes:

- **Customer app** — browse and search the menu, build a cart, choose a pickup time, enter name and
  email, and place an order.
- **Admin app** — see incoming orders, move them through *Preparing → Ready → Completed* (or
  *Cancel*), view a revenue / active-orders snapshot, and switch menu items on or off.

Orders are stored in a plain text file (`data/orders.txt`), which keeps the project dependency-free
and easy to inspect. This is a prototype: there is no payment, and the admin login is a simple
demo-grade check (see below).

## Requirements

- Java JDK 17 or newer
- Maven (JavaFX is pulled in automatically through Maven)

## Run

```bash
mvn clean javafx:run
```

## Test

```bash
mvn test
```

The tests cover order serialization, the text database (including data-safety cases), menu
availability persistence, dashboard statistics, input validation and the admin login check. They do
not need a display.

## Admin login

By default the app uses a **demo login** (`admin` / `omel2026`) and prints a warning at startup.
To choose your own, set these environment variables before starting the app:

```bash
# Windows (cmd)
set OMEL_ADMIN_USER=owner
set OMEL_ADMIN_PASSWORD=choose-a-password

# macOS / Linux
export OMEL_ADMIN_USER=owner
export OMEL_ADMIN_PASSWORD=choose-a-password
```

This protects a desktop prototype from casual use; it is not a substitute for real authentication.

## Data files

Both are created automatically on first run and are git-ignored (they can contain customer names and
emails):

| File | Purpose |
| --- | --- |
| `data/orders.txt` | One order per line |
| `data/menu-state.txt` | Names of menu items the admin has switched off |

`data/orders.sample.txt` shows the format; copy it to `data/orders.txt` to start with sample orders.

### Order line format

```text
ORDER_ID|TIMESTAMP|CUSTOMER_NAME|CUSTOMER_EMAIL|PICKUP_TIME|ITEMS|TOTAL|STATUS|NOTES
```

```text
OMEL-1001|2026-04-28 09:15|Sample Student|sample.student@rit.edu|12:30|Morning Brew Coffee x1, Chicken Caesar Wrap x1|5.70|PENDING|No onions
```

## Design notes

- **MVC with FXML.** `model/` (orders, menu items, cart items, money), `service/` (database, menu,
  statistics, validation, login) and `controller/` (one per screen) are separate; the screens are
  FXML files styled by `premium.css`.
- **Exact money.** Prices and totals are `BigDecimal`, and totals are always written with a dot, so
  orders are read back correctly on computers whose regional settings use a decimal comma.
- **Safe text database.** A line the app cannot read is skipped when listing orders but is never
  deleted or reordered when another order is updated. Updates are written to a temp file and moved
  into place, and `|` / line breaks in free text are neutralised so notes cannot corrupt a record.
- **Input validation.** Name, email and notes length are checked before an order is stored.

## Project structure

```text
src/main/java/com/omel
  OmelApp.java
  controller/   Launch, UserController, AdminLogin, AdminController
  model/        Order, OrderStatus, MenuItem, CartItem, Money
  service/      OrderDatabase, MenuService, OrderStats, Validation, AuthService
src/main/resources/com/omel
  view/         LaunchView, UserView, AdminLoginView, AdminView (.fxml)
  styles/       premium.css
src/test/java/com/omel   unit tests
data/orders.sample.txt
```

## Limitations

- The text file is a prototype database: it is safe within one running app, but two copies of the
  app writing to the same file at once are not coordinated.
- No payment or notifications; pickup times are a fixed list.
- Admin login is demo-grade (see above).

## Authors

Built by Bind Jerliu (customer side) and Art Beqiri (admin side) at RIT Kosovo.
