import { useRef, useState } from 'react';
import Card from './Card';
import { Editor, type OnMount } from '@monaco-editor/react';
import type { editor } from 'monaco-editor';

export default function CodeEditor() {
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

    const editorRef = useRef<editor.IStandaloneCodeEditor | null>(null);

    const handleEditorDidMount: OnMount = (editor) => {
        editorRef.current = editor;
    };

    async function handleSubmit() {
        if (editorRef.current) {
            const code = editorRef.current.getValue();

            try {
                const response = await fetch('http://localhost:8080/enviar', {
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
        <Card className='w-3/7'>
            <div className='p-4 space-y-4 grid grid-rows-[auto_1fr_auto]'>
                <div className="flex space-x-4 overflow-x-auto no-scrollbar">
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

                <div className='rounded-2xl overflow-hidden min-h-140'>
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
    );
}