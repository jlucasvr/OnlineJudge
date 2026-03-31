import { Editor, loader, type OnMount } from '@monaco-editor/react';
import { editor } from 'monaco-editor';
import { useLayoutEffect, useRef, useState } from 'react';

import LangInput from './LangInput';
import Card from './Card';

const langs = [
    {
        label: "C++",
        value: "cpp",
        code: "#include <iostream>\n\nusing namespace std;\n\nint main() {\n\tcout << \"Hello, World!\" << endl;\n\treturn 0;\n}"
    },
    {
        label: "C",
        value: "c",
        code: "#include <stdio.h>\n\nint main() {\n\tprintf(\"Hello, World!\\n\");\n\treturn 0;\n}"
    },
    {
        label: "Python",
        value: "python",
        code: "print(\"Hello, World!\")"
    },
    {
        label: "Java",
        value: "java",
        code: "public class Main {\n\tpublic static void main(String[] args) {\n\t\tSystem.out.println(\"Hello, World!\");\n\t}\n}"
    }
];

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
    const [codeLangs, setCodeLangs] = useState(() => langs.reduce((acc, lang) => {
        acc[lang.value] = lang.code;
        return acc;
    }, {} as Record<string, string>));

    const editorRef = useRef<editor.IStandaloneCodeEditor | null>(null);

    useLayoutEffect(() => {
        if (editorRef.current) {
            editorRef.current.layout();
        }
    });

    const handleEditorDidMount: OnMount = (editor) => {
        editorRef.current = editor;
    };

    async function handleSubmit() {
        if (editorRef.current) {
            const code = editorRef.current.getValue();
            alert(code);

            try {
                const response = await fetch('http://localhost:8080/enviar', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ language, code })
                });

                const data = await response.json();

                if (data.status === 'ok') {
                    alert("Enviado");
                }
            } catch (err) {
                console.error('Erro ao enviar:', err);
            }
        }
    }

    const handleEditorChange = (value: string | undefined) => {
        setCodeLangs(prev => ({
            ...prev,
            [language]: value || ""
        }));
    };

    /*
    const verifyLanguageChange = () => {
        if (!editorRef.current) return false;
        const currentCode = editorRef.current.getValue();
        const defaultCode = langs.find(lang => lang.value === language)?.code;
        if (currentCode !== defaultCode) {
            const confirm = window.confirm("Ao mudar de linguagem seu código atual será perdido.\nTem certeza que deseja fazer essa alteração?");
            if (!confirm) return false;
        }
        return true;
    }
    */

    return (
        <Card className='p-4 space-y-4 grid grid-rows-[auto_1fr_auto] h-full'>
            <div className="flex space-x-4">
                {
                    langs.map((lang) => (
                        <LangInput
                            key={lang.value}
                            label={lang.label}
                            value={lang.value}
                            checked={language === lang.value}
                            onChange={(e) => setLanguage(e.target.value)}
                        />
                    ))
                }
            </div>

            <Card className='rounded-xl overflow-hidden'>
                <Editor
                    height="100%"
                    width="100%"
                    language={language}
                    value={codeLangs[language]}
                    theme="meuTemaPersonalizado"
                    options={{
                        minimap: { enabled: false },
                        automaticLayout: true,
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
                    onChange={handleEditorChange}
                    onMount={handleEditorDidMount}
                    className='absolute inset-0'
                />
            </Card>

            <button
                onClick={handleSubmit}
                className='shadow-bear hover:shadow-laranja/50 py-2 px-4 bg-zinc-50 hover:bg-laranja active:scale-95 transition cursor-pointer rounded-xl text-zinc-950 font-bold justify-self-end'
            >
                Submeter
            </button>
        </Card>
    );
}