/**
 * ====================================================================
 * LIBRARY MANAGEMENT SYSTEM - DASHBOARD CONTROLLER & LOGIC
 * Features: LocalStorage sync, ACID issue/return simulation,
 *           ChronoUnit-style fine calculation, HTML5 Canvas charts,
 *           search/filter, dark/light theme, and interview architect view.
 * ====================================================================
 */

// -------------------------------------------------------------
// 1. INITIAL MOCK SEED DATA
// -------------------------------------------------------------
const INITIAL_BOOKS = [
  { id: 1, isbn: "978-0134685991", title: "Effective Java", author: "Joshua Bloch", category: "Computer Science", year: 2018, available: false, gradient: "linear-gradient(135deg, #1e3a8a, #3b82f6)" },
  { id: 2, isbn: "978-0132350884", title: "Clean Code", author: "Robert C. Martin", category: "Computer Science", year: 2008, available: false, gradient: "linear-gradient(135deg, #065f46, #10b981)" },
  { id: 3, isbn: "978-0596009205", title: "Head First Design Patterns", author: "Eric Freeman", category: "Computer Science", year: 2004, available: true, gradient: "linear-gradient(135deg, #7c2d12, #f97316)" },
  { id: 4, isbn: "978-0321356680", title: "Java Concurrency in Practice", author: "Brian Goetz", category: "Computer Science", year: 2006, available: true, gradient: "linear-gradient(135deg, #4c1d95, #8b5cf6)" },
  { id: 5, isbn: "978-0141439518", title: "Pride and Prejudice", author: "Jane Austen", category: "Literature", year: 1813, available: true, gradient: "linear-gradient(135deg, #831843, #ec4899)" },
  { id: 6, isbn: "978-0451524935", title: "1984", author: "George Orwell", category: "Dystopian Fiction", year: 1949, available: true, gradient: "linear-gradient(135deg, #1f2937, #4b5563)" },
  { id: 7, isbn: "978-0062316097", title: "Sapiens: A Brief History", author: "Yuval Noah Harari", category: "History", year: 2014, available: true, gradient: "linear-gradient(135deg, #78350f, #d97706)" },
  { id: 8, isbn: "978-0735211292", title: "Atomic Habits", author: "James Clear", category: "Self-Help", year: 2018, available: true, gradient: "linear-gradient(135deg, #14532d, #22c55e)" },
  { id: 9, isbn: "978-0262033848", title: "Introduction to Algorithms", author: "Thomas H. Cormen", category: "Algorithms", year: 2009, available: true, gradient: "linear-gradient(135deg, #1e1b4b, #6366f1)" },
  { id: 10, isbn: "978-1449331818", title: "Learning Python", author: "Mark Lutz", category: "Computer Science", year: 2013, available: true, gradient: "linear-gradient(135deg, #0e7490, #06b6d4)" }
];

const INITIAL_MEMBERS = [
  { id: 1, name: "Aarav Sharma", email: "aarav.sharma@example.com", phone: "+91 9876543210", joined: "2025-01-15", limit: 3, status: "ACTIVE" },
  { id: 2, name: "Diya Patel", email: "diya.patel@example.com", phone: "+91 9876543211", joined: "2025-02-10", limit: 3, status: "ACTIVE" },
  { id: 3, name: "Rohan Verma", email: "rohan.verma@example.com", phone: "+91 9876543212", joined: "2025-03-01", limit: 3, status: "ACTIVE" },
  { id: 4, name: "Sneha Reddy", email: "sneha.reddy@example.com", phone: "+91 9876543213", joined: "2025-03-20", limit: 3, status: "ACTIVE" },
  { id: 5, name: "Vikram Joshi", email: "vikram.joshi@example.com", phone: "+91 9876543214", joined: "2025-04-05", limit: 3, status: "ACTIVE" }
];

// Helper to format date offset
function getDateOffset(days) {
  const d = new Date();
  d.setDate(d.getDate() + days);
  return d.toISOString().split("T")[0];
}

const INITIAL_TRANSACTIONS = [
  {
    id: 1,
    bookId: 1,
    bookTitle: "Effective Java",
    memberId: 1,
    memberName: "Aarav Sharma",
    issueDate: getDateOffset(-5),
    dueDate: getDateOffset(9),
    returnDate: null,
    fineAmount: 0.0,
    finePaid: false,
    status: "ISSUED"
  },
  {
    id: 2,
    bookId: 2,
    bookTitle: "Clean Code",
    memberId: 2,
    memberName: "Diya Patel",
    issueDate: getDateOffset(-18),
    dueDate: getDateOffset(-4), // Overdue by 4 days
    returnDate: null,
    fineAmount: 8.0,
    finePaid: false,
    status: "ISSUED"
  },
  {
    id: 3,
    bookId: 3,
    bookTitle: "Head First Design Patterns",
    memberId: 3,
    memberName: "Rohan Verma",
    issueDate: getDateOffset(-25),
    dueDate: getDateOffset(-11),
    returnDate: getDateOffset(-14),
    fineAmount: 0.0,
    finePaid: true,
    status: "RETURNED"
  }
];

// -------------------------------------------------------------
// 2. STATE REPOSITORY (LOCALSTORAGE PERSISTENCE)
// -------------------------------------------------------------
class LibraryStore {
  constructor() {
    this.books = JSON.parse(localStorage.getItem("lms_books")) || INITIAL_BOOKS;
    this.members = JSON.parse(localStorage.getItem("lms_members")) || INITIAL_MEMBERS;
    this.transactions = JSON.parse(localStorage.getItem("lms_transactions")) || INITIAL_TRANSACTIONS;
    this.fineRatePerDay = 2.0; // ₹2/day
    this.borrowDurationDays = 14; // 2 weeks
    this.save();
  }

  save() {
    localStorage.setItem("lms_books", JSON.stringify(this.books));
    localStorage.setItem("lms_members", JSON.stringify(this.members));
    localStorage.setItem("lms_transactions", JSON.stringify(this.transactions));
  }

  reset() {
    localStorage.removeItem("lms_books");
    localStorage.removeItem("lms_members");
    localStorage.removeItem("lms_transactions");
    this.books = [...INITIAL_BOOKS];
    this.members = [...INITIAL_MEMBERS];
    this.transactions = [...INITIAL_TRANSACTIONS];
    this.save();
  }

  getBook(id) { return this.books.find(b => b.id === Number(id)); }
  getMember(id) { return this.members.find(m => m.id === Number(id)); }
  getTransaction(id) { return this.transactions.find(t => t.id === Number(id)); }

  getActiveLoansCount(memberId) {
    return this.transactions.filter(t => t.memberId === Number(memberId) && t.status === "ISSUED").length;
  }

  // Issue Book with ACID rules
  issueBook(bookId, memberId) {
    const book = this.getBook(bookId);
    if (!book || !book.available) throw new Error("Selected book is currently not available for checkout.");

    const member = this.getMember(memberId);
    if (!member || member.status !== "ACTIVE") throw new Error("Member account is invalid or suspended.");

    if (this.getActiveLoansCount(memberId) >= member.limit) {
      throw new Error(`Member has reached maximum borrowing limit of ${member.limit} books.`);
    }

    const todayStr = new Date().toISOString().split("T")[0];
    const dueStr = getDateOffset(this.borrowDurationDays);

    const newTx = {
      id: this.transactions.length > 0 ? Math.max(...this.transactions.map(t => t.id)) + 1 : 1,
      bookId: book.id,
      bookTitle: book.title,
      memberId: member.id,
      memberName: member.name,
      issueDate: todayStr,
      dueDate: dueStr,
      returnDate: null,
      fineAmount: 0.0,
      finePaid: false,
      status: "ISSUED"
    };

    // Atomic update
    book.available = false;
    this.transactions.unshift(newTx);
    this.save();
    return newTx;
  }

  // Return Book with ChronoUnit fine calculation
  returnBook(bookId) {
    const book = this.getBook(bookId);
    if (!book) throw new Error("Book not found.");

    const tx = this.transactions.find(t => t.bookId === book.id && t.status === "ISSUED");
    if (!tx) throw new Error(`No active borrowing record found for '${book.title}'.`);

    const returnDateStr = new Date().toISOString().split("T")[0];
    tx.returnDate = returnDateStr;

    // Overdue Calculation (Days difference)
    const dueDate = new Date(tx.dueDate);
    const returnDate = new Date(returnDateStr);
    const diffTime = returnDate - dueDate;
    const overdueDays = Math.max(0, Math.ceil(diffTime / (1000 * 60 * 60 * 24)));

    if (overdueDays > 0) {
      tx.fineAmount = overdueDays * this.fineRatePerDay;
      tx.status = "RETURNED";
    } else {
      tx.fineAmount = 0.0;
      tx.status = "RETURNED";
      tx.finePaid = true;
    }

    book.available = true;
    this.save();
    return { tx, overdueDays };
  }

  payFine(txId) {
    const tx = this.getTransaction(txId);
    if (!tx) throw new Error("Transaction not found.");
    tx.finePaid = true;
    this.save();
    return tx;
  }

  addBook(data) {
    const newId = this.books.length > 0 ? Math.max(...this.books.map(b => b.id)) + 1 : 1;
    const gradients = [
      "linear-gradient(135deg, #1e3a8a, #3b82f6)",
      "linear-gradient(135deg, #065f46, #10b981)",
      "linear-gradient(135deg, #7c2d12, #f97316)",
      "linear-gradient(135deg, #4c1d95, #8b5cf6)",
      "linear-gradient(135deg, #0e7490, #06b6d4)"
    ];
    const book = {
      id: newId,
      isbn: data.isbn,
      title: data.title,
      author: data.author,
      category: data.category,
      year: Number(data.year),
      available: true,
      gradient: gradients[newId % gradients.length]
    };
    this.books.push(book);
    this.save();
    return book;
  }

  deleteBook(id) {
    const book = this.getBook(id);
    if (!book) throw new Error("Book not found.");
    if (!book.available) throw new Error("Cannot delete a book that is currently issued to a member.");
    this.books = this.books.filter(b => b.id !== Number(id));
    this.save();
  }

  addMember(data) {
    const newId = this.members.length > 0 ? Math.max(...this.members.map(m => m.id)) + 1 : 1;
    const member = {
      id: newId,
      name: data.name,
      email: data.email,
      phone: data.phone,
      joined: new Date().toISOString().split("T")[0],
      limit: Number(data.limit) || 3,
      status: "ACTIVE"
    };
    this.members.push(member);
    this.save();
    return member;
  }
}

const store = new LibraryStore();

// -------------------------------------------------------------
// 3. UI CONTROLLER & VIEW RENDERERS
// -------------------------------------------------------------

function showToast(message, type = "success") {
  const container = document.getElementById("toast-container");
  const toast = document.createElement("div");
  toast.className = `toast ${type}`;
  toast.innerHTML = `
    <span style="font-weight: 700;">${type === 'success' ? '✔' : (type === 'error' ? '✖' : 'ℹ')}</span>
    <span>${message}</span>
  `;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(-10px)';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

// Switch Active View
function switchView(viewName) {
  document.querySelectorAll(".nav-item").forEach(item => {
    item.classList.toggle("active", item.getAttribute("data-view") === viewName);
  });
  document.querySelectorAll(".view-section").forEach(sec => {
    sec.classList.toggle("active", sec.id === `view-${viewName}`);
  });

  if (viewName === "dashboard") renderDashboard();
  if (viewName === "books") renderBooksCatalog();
  if (viewName === "circulation") renderCirculation();
  if (viewName === "members") renderMembers();
  if (viewName === "fines") renderFines();
}

// -------------------------------------------------------------
// 4. VIEW: DASHBOARD & CHARTS
// -------------------------------------------------------------
function renderDashboard() {
  const totalBooks = store.books.length;
  const availableBooks = store.books.filter(b => b.available).length;
  const issuedBooks = totalBooks - availableBooks;
  const totalMembers = store.members.length;

  const activeLoans = store.transactions.filter(t => t.status === "ISSUED");
  const today = new Date();
  const overdueLoans = activeLoans.filter(t => new Date(t.dueDate) < today);

  const pendingFines = store.transactions
    .filter(t => t.fineAmount > 0 && !t.finePaid)
    .reduce((sum, t) => sum + t.fineAmount, 0);

  // Update Stat Counters safely
  const elTotal = document.getElementById("stat-total-books");
  if (elTotal) elTotal.textContent = totalBooks;

  const elAvail = document.getElementById("stat-available-books");
  if (elAvail) elAvail.textContent = availableBooks;

  const elActive = document.getElementById("stat-active-loans");
  if (elActive) elActive.textContent = activeLoans.length;

  const elMembers = document.getElementById("stat-total-members");
  if (elMembers) elMembers.textContent = totalMembers;

  const elOverdue = document.getElementById("stat-overdue-loans");
  if (elOverdue) elOverdue.textContent = overdueLoans.length;

  const elFines = document.getElementById("stat-pending-fines");
  if (elFines) elFines.textContent = `₹${pendingFines.toFixed(2)}`;

  renderBorrowTrendChart();
  renderRecentActivity();
}

function renderBorrowTrendChart() {
  const canvas = document.getElementById("circulationChart");
  if (!canvas || !canvas.parentElement) return;
  const width = canvas.parentElement.clientWidth;
  if (!width || width <= 0) return; // Skip if tab is hidden
  const height = 240;
  canvas.width = width;
  canvas.height = height;

  const ctx = canvas.getContext("2d");
  ctx.clearRect(0, 0, width, height);

  const months = ["May", "Jun", "Jul", "Aug", "Sep", "Oct"];
  const issuedCounts = [14, 22, 19, 28, 35, 42];
  const returnedCounts = [12, 18, 16, 24, 30, 38];

  const maxVal = 50;
  const padding = 40;
  const chartWidth = width - padding * 2;
  const chartHeight = height - padding * 2;
  const stepX = chartWidth / (months.length - 1);

  // Draw Grid Lines
  ctx.strokeStyle = "rgba(255, 255, 255, 0.08)";
  ctx.lineWidth = 1;
  for (let i = 0; i <= 4; i++) {
    const y = padding + (chartHeight / 4) * i;
    ctx.beginPath();
    ctx.moveTo(padding, y);
    ctx.lineTo(width - padding, y);
    ctx.stroke();

    ctx.fillStyle = "#64748b";
    ctx.font = "11px 'Plus Jakarta Sans'";
    ctx.fillText(Math.round(maxVal - (maxVal / 4) * i), 10, y + 4);
  }

  // Draw Line 1 (Issued)
  ctx.beginPath();
  ctx.strokeStyle = "#6366f1";
  ctx.lineWidth = 3;
  issuedCounts.forEach((val, i) => {
    const x = padding + i * stepX;
    const y = padding + chartHeight - (val / maxVal) * chartHeight;
    if (i === 0) ctx.moveTo(x, y);
    else ctx.lineTo(x, y);
  });
  ctx.stroke();

  // Draw Points
  issuedCounts.forEach((val, i) => {
    const x = padding + i * stepX;
    const y = padding + chartHeight - (val / maxVal) * chartHeight;
    ctx.beginPath();
    ctx.arc(x, y, 5, 0, Math.PI * 2);
    ctx.fillStyle = "#6366f1";
    ctx.fill();
    ctx.strokeStyle = "#fff";
    ctx.lineWidth = 2;
    ctx.stroke();

    // Month Label
    ctx.fillStyle = "#94a3b8";
    ctx.fillText(months[i], x - 12, height - 12);
  });
}

function renderRecentActivity() {
  const feed = document.getElementById("recent-activity-feed");
  if (!feed) return;
  const recent = store.transactions.slice(0, 5);

  if (recent.length === 0) {
    feed.innerHTML = `<p style="color: var(--text-muted); font-size: 0.85rem;">No recent circulation activities.</p>`;
    return;
  }

  feed.innerHTML = recent.map(tx => {
    const isReturned = tx.status === "RETURNED";
    const bg = isReturned ? "var(--accent-emerald-light)" : "var(--primary-light)";
    const color = isReturned ? "var(--accent-emerald)" : "var(--primary)";
    const icon = isReturned ? "↺" : "➜";

    return `
      <div class="activity-item">
        <div class="activity-badge" style="background: ${bg}; color: ${color}; font-weight: bold;">${icon}</div>
        <div class="activity-info">
          <h4>${isReturned ? 'Returned: ' : 'Checked out: '}${escapeHtml(tx.bookTitle)}</h4>
          <p>${escapeHtml(tx.memberName)} · Due: ${tx.dueDate}</p>
          <div class="activity-time">${tx.returnDate ? `Returned on ${tx.returnDate}` : 'Currently with member'}</div>
        </div>
      </div>
    `;
  }).join("");
}

// -------------------------------------------------------------
// 5. VIEW: BOOKS CATALOG
// -------------------------------------------------------------
let selectedCategory = "ALL";
let bookSearchQuery = "";

function renderBooksCatalog() {
  const container = document.getElementById("books-grid-container");
  if (!container) return;

  let filtered = store.books;
  if (selectedCategory !== "ALL") {
    filtered = filtered.filter(b => b.category.toLowerCase() === selectedCategory.toLowerCase());
  }

  if (bookSearchQuery.trim()) {
    const q = bookSearchQuery.toLowerCase();
    filtered = filtered.filter(b =>
      b.title.toLowerCase().includes(q) ||
      b.author.toLowerCase().includes(q) ||
      b.isbn.toLowerCase().includes(q) ||
      b.category.toLowerCase().includes(q)
    );
  }

  if (filtered.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 48px; background: var(--bg-card); border-radius: var(--radius-lg); border: 1px dashed var(--border-color);">
        <p style="font-size: 1.1rem; color: var(--text-muted);">No books found matching your filter criteria.</p>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(b => {
    const statusClass = b.available ? "status-available" : "status-issued";
    const statusText = b.available ? "Available" : "Issued";

    return `
      <div class="book-card">
        <div class="book-cover" style="background: ${b.gradient || 'linear-gradient(135deg, #1e1b4b, #3b82f6)'}">
          <div class="book-spine-effect"></div>
          <div class="book-cover-title">${escapeHtml(b.title)}</div>
        </div>
        <div class="book-card-body">
          <div class="book-category">${escapeHtml(b.category)}</div>
          <h3 class="book-title">${escapeHtml(b.title)}</h3>
          <p class="book-author">by ${escapeHtml(b.author)} (${b.year})</p>
          
          <div class="book-meta">
            <span style="color: var(--text-muted); font-size: 0.72rem;">ISBN: ${escapeHtml(b.isbn)}</span>
            <span class="status-pill ${statusClass}">${statusText}</span>
          </div>

          <div class="book-card-actions">
            ${b.available ? `
              <button class="btn btn-primary" onclick="openIssueModal(${b.id})">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
                Issue
              </button>
            ` : `
              <button class="btn btn-success" onclick="openReturnModal(${b.id})">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/></svg>
                Return
              </button>
            `}
            <button class="btn btn-secondary btn-icon" title="Delete Book" onclick="deleteBookAction(${b.id})">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
            </button>
          </div>
        </div>
      </div>
    `;
  }).join("");
}

// -------------------------------------------------------------
// 6. VIEW: CIRCULATION (ISSUE & RETURN LOGS)
// -------------------------------------------------------------
function renderCirculation() {
  const tbody = document.getElementById("circulation-table-body");
  if (!tbody) return;

  const today = new Date();
  tbody.innerHTML = store.transactions.map(tx => {
    const isIssued = tx.status === "ISSUED";
    const isOverdue = isIssued && new Date(tx.dueDate) < today;

    let statusPill = "";
    if (!isIssued) {
      statusPill = `<span class="status-pill status-available">Returned</span>`;
    } else if (isOverdue) {
      statusPill = `<span class="status-pill status-overdue">Overdue</span>`;
    } else {
      statusPill = `<span class="status-pill status-issued">Active</span>`;
    }

    const fineDisplay = tx.fineAmount > 0
      ? `<span style="color: var(--accent-rose); font-weight: 700;">₹${tx.fineAmount.toFixed(2)} ${tx.finePaid ? '<small style="color:var(--accent-emerald)">(Paid)</small>' : '<small>(Pending)</small>'}</span>`
      : `<span style="color: var(--text-muted);">₹0.00</span>`;

    return `
      <tr>
        <td>#${tx.id}</td>
        <td>
          <div class="cell-title">${escapeHtml(tx.bookTitle)}</div>
          <div class="cell-sub">Book ID: #${tx.bookId}</div>
        </td>
        <td>
          <div class="cell-title">${escapeHtml(tx.memberName)}</div>
          <div class="cell-sub">Member ID: #${tx.memberId}</div>
        </td>
        <td>${tx.issueDate}</td>
        <td>
          <span style="${isOverdue ? 'color: var(--accent-rose); font-weight: 700;' : ''}">${tx.dueDate}</span>
        </td>
        <td>${tx.returnDate || '<span style="color: var(--text-muted);">Pending</span>'}</td>
        <td>${fineDisplay}</td>
        <td>${statusPill}</td>
        <td>
          ${isIssued ? `
            <button class="btn btn-secondary" style="padding: 6px 12px; font-size: 0.78rem;" onclick="openReturnModal(${tx.bookId})">
              Check-in
            </button>
          ` : `
            <button class="btn btn-secondary" style="padding: 6px 12px; font-size: 0.78rem; opacity: 0.6;" disabled>
              Completed
            </button>
          `}
        </td>
      </tr>
    `;
  }).join("");
}

// -------------------------------------------------------------
// 7. VIEW: MEMBERS DIRECTORY
// -------------------------------------------------------------
function renderMembers() {
  const container = document.getElementById("members-grid-container");
  if (!container) return;

  container.innerHTML = store.members.map(m => {
    const activeBorrows = store.getActiveLoansCount(m.id);
    const initials = m.name.split(" ").map(n => n[0]).join("").toUpperCase();

    return `
      <div class="card" style="display: flex; flex-direction: column; gap: 14px;">
        <div style="display: flex; align-items: center; gap: 14px;">
          <div class="avatar" style="width: 48px; height: 48px; font-size: 1.1rem;">${initials}</div>
          <div>
            <h3 style="font-size: 1.1rem; font-weight: 700;">${escapeHtml(m.name)}</h3>
            <p style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(m.email)}</p>
          </div>
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; background: var(--bg-input); padding: 12px; border-radius: var(--radius-md); font-size: 0.82rem;">
          <div>
            <span style="color: var(--text-muted); display: block; font-size: 0.7rem; text-transform: uppercase;">Active Loans</span>
            <span style="font-weight: 700; color: ${activeBorrows >= m.limit ? 'var(--accent-rose)' : 'var(--text-primary)'}">
              ${activeBorrows} / ${m.limit}
            </span>
          </div>
          <div>
            <span style="color: var(--text-muted); display: block; font-size: 0.7rem; text-transform: uppercase;">Joined</span>
            <span style="font-weight: 600;">${m.joined}</span>
          </div>
        </div>

        <div style="font-size: 0.8rem; color: var(--text-secondary);">
          <div>📞 ${escapeHtml(m.phone)}</div>
          <div style="margin-top: 4px;">Status: <span style="color: var(--accent-emerald); font-weight: 700;">${m.status}</span></div>
        </div>
      </div>
    `;
  }).join("");
}

// -------------------------------------------------------------
// 8. VIEW: OVERDUE FINES MANAGEMENT
// -------------------------------------------------------------
function renderFines() {
  const tbody = document.getElementById("fines-table-body");
  if (!tbody) return;

  const today = new Date();
  const finesList = store.transactions.filter(t => {
    const isOverdue = t.status === "ISSUED" && new Date(t.dueDate) < today;
    return isOverdue || (t.fineAmount > 0 && !t.finePaid);
  });

  if (finesList.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="6" style="text-align: center; padding: 36px; color: var(--text-muted);">
          🎉 Excellent! No outstanding or overdue fines in the library system.
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = finesList.map(tx => {
    const dueDate = new Date(tx.dueDate);
    const overdueDays = Math.max(1, Math.ceil((today - dueDate) / (1000 * 60 * 60 * 24)));
    const fineCalculated = tx.fineAmount > 0 ? tx.fineAmount : (overdueDays * store.fineRatePerDay);

    return `
      <tr>
        <td>#${tx.id}</td>
        <td>
          <div class="cell-title">${escapeHtml(tx.memberName)}</div>
          <div class="cell-sub">Member #${tx.memberId}</div>
        </td>
        <td>${escapeHtml(tx.bookTitle)}</td>
        <td>${tx.dueDate}</td>
        <td><span style="color: var(--accent-rose); font-weight: 700;">${overdueDays} days</span></td>
        <td><span style="font-weight: 800; color: var(--accent-rose);">₹${fineCalculated.toFixed(2)}</span></td>
        <td>
          <button class="btn btn-primary" style="padding: 6px 14px; font-size: 0.8rem;" onclick="payFineAction(${tx.id}, ${fineCalculated})">
            Collect ₹${fineCalculated.toFixed(2)}
          </button>
        </td>
      </tr>
    `;
  }).join("");
}

// -------------------------------------------------------------
// 9. MODALS & ACTIONS
// -------------------------------------------------------------

function openIssueModal(preselectedBookId = null) {
  const modal = document.getElementById("issue-modal");
  const bookSelect = document.getElementById("issue-book-select");
  const memberSelect = document.getElementById("issue-member-select");

  bookSelect.innerHTML = store.books.filter(b => b.available).map(b =>
    `<option value="${b.id}" ${preselectedBookId === b.id ? 'selected' : ''}>${escapeHtml(b.title)} (${escapeHtml(b.category)})</option>`
  ).join("");

  memberSelect.innerHTML = store.members.filter(m => m.status === "ACTIVE").map(m =>
    `<option value="${m.id}">${escapeHtml(m.name)} (Active: ${store.getActiveLoansCount(m.id)}/${m.limit})</option>`
  ).join("");

  document.getElementById("issue-due-date").value = getDateOffset(store.borrowDurationDays);
  modal.classList.add("active");
}

function handleIssueSubmit(e) {
  e.preventDefault();
  const bookId = Number(document.getElementById("issue-book-select").value);
  const memberId = Number(document.getElementById("issue-member-select").value);

  try {
    const tx = store.issueBook(bookId, memberId);
    closeAllModals();
    showToast(`Book '${tx.bookTitle}' checked out to ${tx.memberName}!`);
    renderDashboard();
    renderBooksCatalog();
    renderCirculation();
  } catch (err) {
    showToast(err.message, "error");
  }
}

function openReturnModal(bookId) {
  try {
    const { tx, overdueDays } = store.returnBook(bookId);
    renderDashboard();
    renderBooksCatalog();
    renderCirculation();
    renderFines();

    if (overdueDays > 0) {
      showToast(`Book returned! OVERDUE by ${overdueDays} days. Fine calculated: ₹${tx.fineAmount.toFixed(2)}`, "error");
    } else {
      showToast(`Book '${tx.bookTitle}' returned on time! No fine applied.`, "success");
    }
  } catch (err) {
    showToast(err.message, "error");
  }
}

function payFineAction(txId, amount) {
  store.payFine(txId);
  showToast(`Payment of ₹${amount.toFixed(2)} collected successfully! Receipt generated.`);
  renderDashboard();
  renderFines();
  renderCirculation();
}

function deleteBookAction(id) {
  const b = store.getBook(id);
  if (!b) return;
  if (confirm(`Are you sure you want to delete '${b.title}'?`)) {
    try {
      store.deleteBook(id);
      showToast(`Book '${b.title}' deleted successfully.`);
      renderDashboard();
      renderBooksCatalog();
    } catch (err) {
      showToast(err.message, "error");
    }
  }
}

function openAddBookModal() {
  document.getElementById("add-book-form").reset();
  document.getElementById("add-book-modal").classList.add("active");
}

function handleAddBookSubmit(e) {
  e.preventDefault();
  const form = document.getElementById("add-book-form");
  const data = {
    isbn: form.isbn.value.trim(),
    title: form.title.value.trim(),
    author: form.author.value.trim(),
    category: form.category.value.trim(),
    year: form.year.value.trim()
  };

  try {
    const newBook = store.addBook(data);
    closeAllModals();
    showToast(`Book '${newBook.title}' added to catalog!`);
    renderDashboard();
    renderBooksCatalog();
  } catch (err) {
    showToast(err.message, "error");
  }
}

function openAddMemberModal() {
  document.getElementById("add-member-form").reset();
  document.getElementById("add-member-modal").classList.add("active");
}

function handleAddMemberSubmit(e) {
  e.preventDefault();
  const form = document.getElementById("add-member-form");
  const data = {
    name: form.name.value.trim(),
    email: form.email.value.trim(),
    phone: form.phone.value.trim(),
    limit: form.limit.value.trim()
  };

  try {
    const newMember = store.addMember(data);
    closeAllModals();
    showToast(`Member '${newMember.name}' registered successfully!`);
    renderDashboard();
    renderMembers();
  } catch (err) {
    showToast(err.message, "error");
  }
}

function closeAllModals() {
  document.querySelectorAll(".modal-overlay").forEach(m => m.classList.remove("active"));
}

function escapeHtml(str) {
  if (!str) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

// -------------------------------------------------------------
// 10. EVENT LISTENERS & INITIALIZATION
// -------------------------------------------------------------
document.addEventListener("DOMContentLoaded", () => {
  // Navigation
  document.querySelectorAll(".nav-item").forEach(item => {
    item.addEventListener("click", (e) => {
      e.preventDefault();
      const view = item.getAttribute("data-view");
      switchView(view);
    });
  });

  // Global Search input
  const searchInput = document.getElementById("global-search");
  if (searchInput) {
    searchInput.addEventListener("input", (e) => {
      bookSearchQuery = e.target.value;
      if (document.getElementById("view-books").classList.contains("active")) {
        renderBooksCatalog();
      } else {
        switchView("books");
      }
    });
  }

  // Filter chips in Catalog
  document.querySelectorAll(".filter-chip").forEach(chip => {
    chip.addEventListener("click", () => {
      document.querySelectorAll(".filter-chip").forEach(c => c.classList.remove("active"));
      chip.classList.add("active");
      selectedCategory = chip.getAttribute("data-cat");
      renderBooksCatalog();
    });
  });

  // Theme toggle
  const themeBtn = document.getElementById("theme-toggle-btn");
  if (themeBtn) {
    const currentTheme = localStorage.getItem("lms_theme") || "dark";
    document.documentElement.setAttribute("data-theme", currentTheme);

    themeBtn.addEventListener("click", () => {
      const active = document.documentElement.getAttribute("data-theme");
      const target = active === "dark" ? "light" : "dark";
      document.documentElement.setAttribute("data-theme", target);
      localStorage.setItem("lms_theme", target);
      renderBorrowTrendChart();
    });
  }

  // Forms
  document.getElementById("issue-form")?.addEventListener("submit", handleIssueSubmit);
  document.getElementById("add-book-form")?.addEventListener("submit", handleAddBookSubmit);
  document.getElementById("add-member-form")?.addEventListener("submit", handleAddMemberSubmit);

  // Close modals on overlay click or Esc key
  document.querySelectorAll(".modal-overlay").forEach(overlay => {
    overlay.addEventListener("click", (e) => {
      if (e.target === overlay) closeAllModals();
    });
  });

  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") closeAllModals();
    if (e.key === "/" && document.activeElement.tagName !== "INPUT") {
      e.preventDefault();
      searchInput?.focus();
    }
  });

  // Initial render
  renderDashboard();
});
