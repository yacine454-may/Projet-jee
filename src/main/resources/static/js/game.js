let firstCard = null;
let isLocked = false;
let timerInterval = null;
let seconds = 0;

function startTimer() {
    timerInterval = setInterval(() => {
        seconds++;
        const m = String(Math.floor(seconds / 60)).padStart(2, "0");
        const s = String(seconds % 60).padStart(2, "0");
        const el = document.getElementById("timer");
        if (el) {
            el.textContent = m + ":" + s;
        }
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
    if (!i1 || !i2 || !form) {
        return;
    }

    i1.value = String(previousCard.index);
    i2.value = String(secondCard.index);

    setTimeout(() => form.submit(), 250);
}

document.addEventListener("DOMContentLoaded", () => {
    initCards();
    const modal = document.getElementById("game-over-modal");
    if (modal && modal.style.display !== "flex") {
        startTimer();
    } else if (timerInterval) {
        clearInterval(timerInterval);
    }
});
