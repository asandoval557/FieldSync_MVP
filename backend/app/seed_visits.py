from app.db import SessionLocal
from app.models.user import User
from app.models.store import Store
from app.models.visit_history import Visit
from datetime import datetime, timedelta
import uuid



db = SessionLocal()

store = Store(
    id=str(uuid.uuid4()),
    name="Staples",
    address="123 Main St, San Antonio, TX"
)

store2 = Store(
    id=str(uuid.uuid4()),
    name="Target",
    address="456 Elm St, Austin, TX"
)

store3 = Store(
    id=str(uuid.uuid4()),
    name="Best Buy",
    address="789 Oak St, Dallas, TX"
)


user = User(
    id=str("f98f31a9-ab9f-416f-89b0-39531122b9a9"),
    email="testuser@example.com"
)


visit1_check_in = datetime(2025, 9, 21, 2, 44)  # UTC
visit1_check_out = visit1_check_in + timedelta(hours=1)


visit2_check_in = datetime(2025, 9, 19, 17, 44)
visit2_check_out = visit2_check_in + timedelta(hours=2, minutes=30)


visit3_check_in = datetime(2025, 9, 18, 12, 44)
visit3_check_out = visit3_check_in + timedelta(hours=3, minutes=37)


visit = Visit(
    id=str(uuid.uuid4()),
    user_id=user.id,
    store_id=store.id,
    check_in_time=visit1_check_in,
    check_out_time=visit1_check_out,
    visit_purpose="Routine check",
    compliance_status="Completed",
    notes="All good"
)

visit2 = Visit(
    id=str(uuid.uuid4()),
    user_id=user.id,
    store_id=store2.id,
    check_in_time=visit2_check_in,
    check_out_time=visit2_check_out,
    visit_purpose="Inventory audit",
    compliance_status="Completed",
    notes="Minor discrepancies"
)

visit3 = Visit(
    id=str(uuid.uuid4()),
    user_id=user.id,
    store_id=store3.id,
    check_in_time=visit3_check_in,
    check_out_time=visit3_check_out,
    visit_purpose="Inventory audit",
    compliance_status="Completed",
    notes="Passed all checks"
)


db.add_all([
    user,
    store, store2, store3,
    visit, visit2, visit3
])

db.commit()
print("Seeded user ID:", user.id)
print("Seeded store ID:", store.id)
print("Seeded visit ID:", visit.id)
db.close()

