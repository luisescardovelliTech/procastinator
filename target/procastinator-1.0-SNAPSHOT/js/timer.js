const TIMER_CONFIG = {
    descansoSegundos: 25 * 60,
    produtividadeSegundos: 5 * 60
};
const API = {
    tarefas: `${APP_CONTEXT}/api/tarefas`
};

let timerRodando = false;
let timerInterval = null;
let faseAtual = "DESCANSO";
let segundosRestantes = TIMER_CONFIG.descansoSegundos;
let segundosFaseAtual = TIMER_CONFIG.descansoSegundos;

function formatarDataPtBr(dataIso) {
    if (!dataIso) {
        return "--/--/----";
    }
    const data = new Date(`${dataIso}T00:00:00`);
    if (Number.isNaN(data.getTime())) {
        return "--/--/----";
    }
    return data.toLocaleDateString("pt-BR");
}

function preencherResumoTarefas(tarefas) {
    const lista = Array.isArray(tarefas) ? tarefas : [];
    const pendentes = lista.filter(function (t) {
        return t.status !== "QUASE_FIZ";
    }).length;

    const prazosValidos = lista
        .map(function (t) { return t.dataPrazo; })
        .filter(Boolean)
        .sort();

    const proximoPrazo = prazosValidos.length ? prazosValidos[0] : null;

    $("#timer-pendentes").text(`${pendentes} Pendentes`);
    $("#timer-acoes").text(`${lista.length} "Amanha"`);
    $("#timer-proximo-prazo").text(formatarDataPtBr(proximoPrazo));
}

function carregarResumoTarefas() {
    $.get(API.tarefas)
        .done(preencherResumoTarefas)
        .fail(function () {
            $("#timer-pendentes").text("0 Pendentes");
            $("#timer-acoes").text('0 "Amanha"');
            $("#timer-proximo-prazo").text("--/--/----");
        });
}

function formatarTempo(totalSegundos) {
    const minutos = Math.floor(totalSegundos / 60);
    const segundos = totalSegundos % 60;
    return `${String(minutos).padStart(2, "0")}:${String(segundos).padStart(2, "0")}`;
}

function atualizarProgresso() {
    const progresso = (segundosFaseAtual - segundosRestantes) / segundosFaseAtual;
    const angulo = Math.max(0, Math.min(360, progresso * 360));
    $("#timer-ring").css("--timer-progress", `${angulo}deg`);
}

function atualizarUI() {
    const ehDescanso = faseAtual === "DESCANSO";
    $("#timer-display").text(formatarTempo(segundosRestantes));
    $("#timer-phase").text(ehDescanso ? "Fase de Descanso" : "Fase de Produtividade");
    $("#btn-timer-start").text(timerRodando ? "Pausar" : (ehDescanso ? "Comecar Descanso" : "Comecar Produtividade"));
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
    atualizarUI();
}

function iniciarTimer() {
    if (timerInterval) {
        clearInterval(timerInterval);
    }
    timerRodando = true;
    atualizarUI();

    timerInterval = setInterval(function () {
        segundosRestantes -= 1;
        if (segundosRestantes <= 0) {
            alternarFase();
        }
        atualizarUI();
    }, 1000);
}

function pausarTimer() {
    timerRodando = false;
    if (timerInterval) {
        clearInterval(timerInterval);
        timerInterval = null;
    }
    atualizarUI();
}

function resetarTimer() {
    pausarTimer();
    faseAtual = "DESCANSO";
    segundosFaseAtual = TIMER_CONFIG.descansoSegundos;
    segundosRestantes = segundosFaseAtual;
    atualizarUI();
}

$(function () {
    carregarResumoTarefas();
    atualizarUI();

    $("#btn-timer-start").on("click", function () {
        if (timerRodando) {
            pausarTimer();
            return;
        }
        iniciarTimer();
    });

    $("#btn-timer-reset").on("click", function () {
        resetarTimer();
    });
});
