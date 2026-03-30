import { useRef, useState } from 'react';
import Problem, { type ProblemProps } from './Problem';
import './App.css';
import Card from './Card';
import { Editor, type OnMount } from '@monaco-editor/react';
import type { editor } from 'monaco-editor';

export default function App() {
  const [size, setSize] = useState(70);
  const [language, setLanguage] = useState('javascript');

  const langs = [
    { label: "C++", value: "cpp" },
    { label: "C", value: "c" },
    { label: "Python", value: "python" },
    { label: "Java", value: "java" },
    { label: "Go", value: "go" },
    { label: "Javascript", value: "javascript" },
    { label: "Typescript", value: "typescript" }
  ]

  const problem: ProblemProps = {
    title: "Extremamente Básico",
    source: {
      autor: "Neilor Tonin",
      instituicao: "URI",
      localizacao: "Brasil",
    },
    content: 'Leia 2 valores inteiros e armazene-os nas variáveis A e B. Efetue a soma de A e B atribuindo o seu resultado na variável X. Imprima X conforme exemplo apresentado abaixo. Não apresente mensagem alguma além daquilo que está sendo especificado e não esqueça de imprimir o fim de linha após o resultado, caso contrário, você receberá "Presentation Error".',
    timeLimit: 1,
    in: "A entrada contém 2 valores inteiros.",
    out: 'Imprima a mensagem "X = " (letra X maiúscula) seguido pelo valor da variável X e pelo final de linha. Cuide para que tenha um espaço antes e depois do sinal de igualdade, conforme o exemplo abaixo.',
    example: [
      {
        in: "10\n9\n",
        out: "X = 19"
      }
    ]
  }

  const editorRef = useRef<editor.IStandaloneCodeEditor | null>(null);

  const handleEditorDidMount: OnMount = (editor) => {
    editorRef.current = editor;
  };

  async function handleSubmit() {
    if (editorRef.current) {
      const code = editorRef.current.getValue();

      try {
        const response = await fetch('http://localhost:8000/enviar', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ language, code })
        });

        const data = await response.json();

        if (data.status === 'ok') {
          alert("enviado");
        }
      } catch (err) {
        console.error('Erro ao enviar:', err);
      }
    }
  }

  return (
    <div className='grid grid-rows-[auto_1fr_auto] grid-cols-[auto_1fr] min-h-screen bg-zinc-900 text-zinc-50'>

      <header className='h-20 col-span-2 flex items-center space-x-4 p-4'>
        <button
          className='p-2 bg-zinc-50 hover:bg-laranja active:scale-95 transition cursor-pointer rounded-xl text-2xl text-zinc-950 font-bold'
          onClick={() => setSize(size === 70 ? 200 : 70)}
        >
          {size === 70 ? <svg xmlns="http://www.w3.org/2000/svg" width="1em" height="1em" viewBox="0 0 24 24"><path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="m7 18l6-6l-6-6m10 0v12" /></svg> : <svg xmlns="http://www.w3.org/2000/svg" width="1em" height="1em" viewBox="0 0 24 24"><path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="m17 18l-6-6l6-6M7 6v12" /></svg>}
        </button>

        <span className='text-3xl text-laranja font-black'>Pumpk<span className='text-roxo'>in</span> Code</span>
      </header>

      <aside
        style={{ width: `${size}px` }}
        className="transition-all duration-300 overflow-hidden"
      >
      </aside>

      <main className='p-10 flex space-x-10 overflow-y-auto rounded-l-[48px] bg-zinc-950'>
        <Problem problem={problem} />

        <Card className='w-2/5'>
          <div className='p-4 space-y-4 grid grid-rows-[auto_1fr_auto]'>
            <div className="flex space-x-4 overflow-x-auto no-scrollbar rounded-xl">
              {langs.map((lang) => (
                <label key={lang.value} className="relative">
                  <input
                    type="radio"
                    name="lang"
                    value={lang.value}
                    checked={language === lang.value}
                    onChange={(e) => setLanguage(e.target.value)}
                    className="peer hidden"
                  />
                  <div className="px-4 py-2 font-bold bg-zinc-800 border border-zinc-700 rounded-xl cursor-pointer
                      peer-checked:border-laranja peer-checked:text-laranja peer-checked:bg-laranja/5 transition-all">
                    {lang.label}
                  </div>
                </label>
              ))}
            </div>

            <div className='rounded-2xl overflow-hidden min-h-175'>
              <Editor
                height="100%"
                language={language}
                defaultValue={"// coloque aqui o seu código"}
                theme="vs-dark"
                options={{
                  minimap: { enabled: false },
                  fontSize: 16,
                  cursorStyle: 'line',
                  wordWrap: 'on',
                  fontFamily: "JetBrains Mono",
                  fontWeight: "500",
                  fontLigatures: "true",
                }}
                onMount={handleEditorDidMount}
              />
            </div>

            <button onClick={handleSubmit} className='py-2 px-4 bg-zinc-50 hover:bg-laranja active:scale-95 transition cursor-pointer rounded-xl text-zinc-950 font-bold justify-self-end'>Submeter</button>
          </div>
        </Card>
      </main >

      <footer className='h-20 col-span-2'>
      </footer>
    </div >
  );
}