let firstCard = null;
let isLocked = false;
let timerInterval = null;
let seconds = 0;

function formatTime(s) {
    return String(Math.floor(s / 60)).padStart(2, "0") + ":" + String(s % 60).padStart(2, "0");
}

function startTimer() {
    timerInterval = setInterval(() => {
        seconds++;
        const el = document.getElementById("timer");
        if (el) el.textContent = formatTime(seconds);
    }, 1000);
}

function initCards() {
    document.querySelectorAll(".card:not(.matched)").forEach((card) => {
        card.addEventListener("click", handleCardClick);
    });
}

function handleCardClick() {
    const card = this;
    const index = Number.parseInt(card.dataset.index, 10);

    if (isLocked || card.classList.contains("flipped") || card.classList.contains("matched")) {
        return;
    }

    card.classList.add("flipped");

    if (firstCard === null) {
        firstCard = { card, index };
        return;
    }

    const secondCard = { card, index };
    const previousCard = firstCard;
    firstCard = null;
    isLocked = true;

    const i1 = document.getElementById("flip-i1");
    const i2 = document.getElementById("flip-i2");
    const form = document.getElementById("flip-form");
    if (!i1 || !i2 || !form) return;

    i1.value = String(previousCard.index);
    i2.value = String(secondCard.index);

    setTimeout(() => {
        const movesEl = document.getElementById("moves");
        sessionStorage.setItem("gameTimer", String(seconds));
        sessionStorage.setItem("gameTimerMoves", movesEl ? movesEl.textContent.trim() : "0");
        form.submit();
    }, 250);
}

document.addEventListener("DOMContentLoaded", () => {
    const modal = document.getElementById("game-over-modal");
    if (modal && modal.style.display === "flex") {
        sessionStorage.removeItem("gameTimer");
        sessionStorage.removeItem("gameTimerMoves");
        return;
    }

    const movesEl = document.getElementById("moves");
    const currentMoves = movesEl ? parseInt(movesEl.textContent.trim(), 10) : 0;
    const savedMoves   = parseInt(sessionStorage.getItem("gameTimerMoves") || "-1", 10);
    const savedSecs    = parseInt(sessionStorage.getItem("gameTimer")      || "0",  10);

    if (currentMoves === 0) {
        seconds = 0;
        sessionStorage.removeItem("gameTimer");
        sessionStorage.removeItem("gameTimerMoves");
    } else if (currentMoves === savedMoves + 1) {
        seconds = savedSecs;
    } else {
        seconds = 0;
        sessionStorage.removeItem("gameTimer");
        sessionStorage.removeItem("gameTimerMoves");
    }

    const timerEl = document.getElementById("timer");
    if (timerEl) timerEl.textContent = formatTime(seconds);

    initCards();
    startTimer();
});
