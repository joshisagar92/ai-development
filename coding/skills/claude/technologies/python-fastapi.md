---
name: python-fastapi
description: Python and FastAPI best practices. Use when writing Python APIs, FastAPI applications, or Python web services.
---

# Python & FastAPI

> **Domain Expert**: Sebastian Ramirez (@tiangolo, FastAPI Creator)

## Use Pydantic Models

Automatic validation, serialization, and documentation.

```python
from pydantic import BaseModel, EmailStr, Field

class UserCreate(BaseModel):
    email: EmailStr
    name: str
    age: int = Field(ge=0, le=150)

@app.post("/users/", response_model=UserResponse)
async def create_user(user: UserCreate):
    return user
```

## Use Dependency Injection

FastAPI's `Depends()` for database sessions, auth, and shared logic.

```python
from fastapi import Depends
from sqlalchemy.orm import Session

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.get("/users/{user_id}")
async def get_user(user_id: int, db: Session = Depends(get_db)):
    return db.query(User).filter(User.id == user_id).first()
```

## Use async/await Appropriately

Use `async def` for I/O-bound; `def` for CPU-bound.

```python
# Good: async for database/network operations
@app.get("/items/")
async def get_items(db: AsyncSession = Depends(get_async_db)):
    result = await db.execute(select(Item))
    return result.scalars().all()

# Good: sync for CPU-bound operations
@app.get("/compute/")
def compute_heavy():
    return expensive_computation()
```

## Organize Routes with APIRouter

Split large applications into routers by domain.

```python
# routers/users.py
from fastapi import APIRouter
router = APIRouter(prefix="/users", tags=["users"])

@router.get("/")
async def list_users():
    pass

# main.py
from routers import users, orders
app.include_router(users.router)
app.include_router(orders.router)
```

## Use HTTPException for Error Handling

Consistent error responses with proper status codes.

```python
from fastapi import HTTPException, status

@app.get("/items/{item_id}")
async def get_item(item_id: int):
    item = db.get(item_id)
    if not item:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Item {item_id} not found"
        )
    return item
```

## Use Background Tasks

Don't block responses for non-critical work.

```python
from fastapi import BackgroundTasks

def send_email(email: str, message: str):
    # Email sending logic
    pass

@app.post("/signup/")
async def signup(email: str, background_tasks: BackgroundTasks):
    background_tasks.add_task(send_email, email, "Welcome!")
    return {"message": "Signed up successfully"}
```

## Configure CORS Properly

Explicit origins, not wildcards in production.

```python
from fastapi.middleware.cors import CORSMiddleware

app.add_middleware(
    CORSMiddleware,
    allow_origins=["https://myapp.com"],
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE"],
    allow_headers=["*"],
)
```

## Quick Reference

| Practice | Rule |
|----------|------|
| Validation | Use Pydantic models |
| DI | Use Depends() for shared logic |
| Async | async for I/O, sync for CPU |
| Organization | APIRouter by domain |
| Errors | HTTPException with status codes |
| Background | BackgroundTasks for non-blocking |
| CORS | Explicit origins in production |
