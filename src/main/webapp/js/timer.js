const TIMER_CONFIG = { descansoSegundos: 25 * 60, produtividadeSegundos: 5 * 60 };
let timerRodando = false, timerInterval = null, faseAtual = "DESCANSO";
let segundosRestantes = TIMER_CONFIG.descansoSegundos, segundosFaseAtual = TIMER_CONFIG.descansoSegundos;

function formatarTempo(s) {
    return String(Math.floor(s / 60)).padStart(2, "0") + ":" + String(s % 60).padStart(2, "0");
}

function atualizarProgresso() {
    const p = (segundosFaseAtual - segundosRestantes) / segundosFaseAtual;
    $("#timer-ring").css("--timer-progress", Math.max(0, Math.min(360, p * 360)) + "deg");
}

function atualizarUI() {
    const desc = faseAtual === "DESCANSO";
    $("#timer-display").text(formatarTempo(segundosRestantes));
    $("#timer-phase").text(desc ? "Fase de Descanso" : "Fase de Produtividade");
    $("#btn-timer-start").text(timerRodando ? "Pausar" : (desc ? "Comecar Descanso" : "Comecar Produtividade"));
    atualizarProgresso();
}

function alternarFase() {
    if (faseAtual === "DESCANSO") {
        faseAtual = "PRODUTIVIDADE";
        segundosFaseAtual = TIMER_CONFIG.produtividadeSegundos;
    } else {
        faseAtual = "DESCANSO";
        segundosFaseAtual = TIMER_CONFIG.descansoSegundos;
    }
    segundosRestantes = segundosFaseAtual;
}

$(function () {
    atualizarUI();
    $("#btn-timer-start").on("click", function () {
        if (timerRodando) {
            timerRodando = false;
            clearInterval(timerInterval);
            timerInterval = null;
            atualizarUI();
            return;
        }
        timerRodando = true;
        atualizarUI();
        timerInterval = setInterval(function () {
            segundosRestantes--;
            if (segundosRestantes <= 0) { alternarFase(); }
            atualizarUI();
        }, 1000);
    });
    $("#btn-timer-reset").on("click", function () {
        timerRodando = false;
        clearInterval(timerInterval);
        faseAtual = "DESCANSO";
        segundosFaseAtual = TIMER_CONFIG.descansoSegundos;
        segundosRestantes = segundosFaseAtual;
        atualizarUI();
    });
});
