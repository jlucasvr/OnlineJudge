import Card from "./Card";

export interface ProblemProps {
    title: string;
    source?: {
        autor?: string;
        instituicao: string;
        localizacao: "Brasil" | "EUA";
    };
    in?: string;
    out: string;
    timeLimit: number;
    content: string;
    example: {
        in?: string;
        out: string;
    }[];
}

interface Props {
    problem: ProblemProps;
}

export default function Problem({ problem }: Props) {
    return (
        <Card className="w-3/5 ">
            <article className='grid grid-rows-[auto_1fr] h-full w-full font-medium'>
                <header className='p-4 bg-zinc-950/50 text-center'>
                    <h1 className='font-bold text-4xl'>{problem.title}</h1>
                    {problem.source && <div className="text-base">{problem.source.autor && problem.source.autor + ", "} {problem.source.instituicao} - {problem.source.localizacao}</div>}
                    <span className="font-bold">Timelimit: {problem.timeLimit}</span>
                </header>
                <div className='flex flex-col p-4 space-y-4'>
                    <div className="space-y-4 h-full">
                        <pre className='text-justify grow text-wrap font-sans'>{problem.content}</pre>
                        <div className="space-y-2">
                            <h2 className="text-2xl font-bold">Entrada</h2>
                            <pre className='text-justify grow text-wrap font-sans'>{problem.in}</pre>
                        </div>
                        <div className="space-y-2">
                            <h2 className="text-2xl font-bold">Entrada</h2>
                            <pre className='text-justify grow text-wrap font-sans'>{problem.out}</pre>
                        </div>
                    </div>
                    {
                        problem.example && problem.example.map((ex) => (
                            <div className='flex space-x-4'>
                                {ex.in && <Pre codigo={ex.in} titulo="Exemplo de Entrada" />}
                                <Pre codigo={ex.out} titulo="Exemplo de Saída" />
                            </div>
                        ))
                    }
                </div>
            </article>
        </Card >
    )
}

interface PreProps {
    codigo: string;
    titulo: string;
}

function Pre({ codigo, titulo }: PreProps) {

    return (
        <div className='border border-zinc-700 rounded-xl overflow-hidden w-full shadow-md shadow-zinc-800'>
            <h3 className='p-4 bg-zinc-950/50 text-xl'>{titulo}</h3>
            <pre className='p-4'>{codigo}</pre>
        </div>
    )
}