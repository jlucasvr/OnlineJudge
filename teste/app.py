from flask import Flask, request, jsonify, render_template
import subprocess
import os

app = Flask(__name__)
BASE = os.path.dirname(os.path.abspath(__file__))  # ← fora das funções

@app.route('/sucesso')
def sucesso():
    with open(os.path.join(BASE, 'saida.txt'), 'r', encoding='utf-8') as f:
        conteudo = f.read()
    return f"<h1>Resultado:</h1><pre>{conteudo}</pre>"

@app.route('/')
def index():
    return render_template('code-submit.html')

@app.route('/enviar', methods=['POST'])
def receber_codigo():
    dados = request.get_json()
    linguagem = dados.get('lang', 'Outro')
    codigo = dados.get('code', '')

    CPP = os.path.join(BASE, 'arquivo.c')
    EXE = os.path.join(BASE, 'programa')

    with open(CPP, 'w') as f:
        f.write(codigo)

    compile = subprocess.run(
        ['gcc', CPP, '-o', EXE],
        capture_output=True,
        text=True
    )

    if compile.returncode != 0:
        return jsonify({ "status": "erro", "resultado": compile.stderr })

    run = subprocess.run(
        [EXE],
        capture_output=True,
        text=True,
        timeout=5
    )

    with open(os.path.join(BASE, 'saida.txt'), 'w') as f:
        f.write(run.stdout)

    return jsonify({ "status": "ok", "resultado": run.stdout })

if __name__ == '__main__':
    app.run(debug=True, port=5000)