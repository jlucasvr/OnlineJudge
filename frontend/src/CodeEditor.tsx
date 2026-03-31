import { useRef, useState } from 'react';
import Card from './Card';
import { Editor, loader, type OnMount } from '@monaco-editor/react';
import { editor } from 'monaco-editor';

loader.init().then((monaco) => {
    monaco.editor.defineTheme('meuTemaPersonalizado', {
        base: 'vs-dark',
        inherit: true,
        rules: [],
        colors: {
            'editor.background': '#111113',
        },
    });
});

export default function CodeEditor() {
    const [language, setLanguage] = useState('cpp');

    const langs = [
        { label: "C++", value: "cpp", code: "#include <iostream>\n\nusing namespace std;\n\nint main() {\n\tcout << \"Hello, World!\" << endl;\n\treturn 0;\n}" },
        { label: "C", value: "c", code: "#include <stdio.h>\n\nint main() {\n\tprintf(\"Hello, World!\\n\");\n\treturn 0;\n}" },
        { label: "Python", value: "python", code: "print(\"Hello, World!\")" },
        { label: "Java", value: "java", code: "public class Main {\n\tpublic static void main(String[] args) {\n\t\tSystem.out.println(\"Hello, World!\");\n\t}\n}" }
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
        <Card className='w-3/7 p-4 space-y-4 grid grid-rows-[auto_1fr_auto]'>
            <div className="flex space-x-4">
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
                        <div className="px-4 py-2 font-bold bg-zinc-800/10 border border-zinc-700/75 shadow-bear rounded-xl cursor-pointer
                      peer-checked:border-laranja peer-checked:text-laranja peer-checked:bg-laranja/10 hover:border-laranja hover:text-laranja hover:bg-laranja/10 transition-all duration-300">
                            {lang.label}
                        </div>
                    </label>
                ))}
            </div>

            <Card className='rounded-xl overflow-hidden min-h-[60svh]'>
                <code>
                    <Editor
                        height="100%"
                        language={language}
                        value={langs.find(lang => lang.value === language)?.code}
                        theme="meuTemaPersonalizado"
                        options={{
                            minimap: { enabled: false },
                            fontSize: 16,
                            cursorStyle: 'line',
                            wordWrap: 'on',
                            fontFamily: "JetBrains Mono",
                            fontWeight: "500",
                            fontLigatures: "true",
                            padding: {
                                top: 10,
                                bottom: 10
                            },
                        }}
                        onMount={handleEditorDidMount}
                    />
                </code>
            </Card>

            <button onClick={handleSubmit} className='shadow-bear hover:shadow-laranja/50 py-2 px-4 bg-zinc-50 hover:bg-laranja active:scale-95 transition cursor-pointer rounded-xl text-zinc-950 font-bold justify-self-end'>Submeter</button>
        </Card>
    );
}