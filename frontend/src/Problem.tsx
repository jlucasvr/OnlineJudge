import Card from "./Card";

type Country =
    | "Alemanha"
    | "Austrália"
    | "Áustria"
    | "Bélgica"
    | "Brasil"
    | "Canadá"
    | "China"
    | "Chipre"
    | "Coreia do Sul"
    | "Dinamarca"
    | "Emirados Árabes Unidos"
    | "Espanha"
    | "Estados Unidos"
    | "Estônia"
    | "Finlândia"
    | "França"
    | "Hong Kong"
    | "Índia"
    | "Irlanda"
    | "Islândia"
    | "Israel"
    | "Itália"
    | "Japão"
    | "Noruega"
    | "Países Baixos"
    | "Polônia"
    | "Reino Unido"
    | "Singapura"
    | "Suécia"
    | "Suíça"
    | "Taiwan";

export interface ProblemProps {
    title: string;
    source?: {
        autor?: string;
        instituicao: string;
        localizacao: Country;
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
        <Card className="w-4/7">
            <article className='grid p-4 grid-rows-[auto_1fr] h-full w-full font-medium space-y-4'>

                <Card className="rounded-xl">
                    <header className='p-4 bg-zinc-950/50 text-center'>
                        <h1 className='font-bold text-4xl'>{problem.title}</h1>

                        {problem.source && <div className="text-base">{problem.source.autor && problem.source.autor + ", "} {problem.source.instituicao} - {problem.source.localizacao}</div>}

                        <span className="font-bold">Timelimit: {problem.timeLimit}</span>
                    </header>
                </Card>

                <div className="space-y-4 h-full">
                    <pre className='text-justify grow text-wrap font-sans'>{problem.content}</pre>

                    <div className="space-y-2">
                        <h2 className="text-2xl font-bold">Entrada</h2>
                        <pre className='text-justify grow text-wrap font-sans'>{problem.in}</pre>
                    </div>

                    <div className="space-y-2">
                        <h2 className="text-2xl font-bold">Saída</h2>
                        <pre className='text-justify grow text-wrap font-sans'>{problem.out}</pre>
                    </div>
                </div>

                <div className='flex flex-col space-y-4'>
                    <div className='flex space-x-4'>
                        <h3 className='text-2xl font-bold w-1/2'>Exemplo de Entrada</h3>
                        <h3 className='text-2xl font-bold w-1/2'>Exemplo de Saída</h3>
                    </div>

                    {
                        problem.example && problem.example.map((ex) => (
                            <Card className='flex space-x-4 rounded-xl bg-zinc-950/50'>
                                {ex.in && <pre className='p-4 w-1/2'>{ex.in}</pre>}
                                <div className="border-l border-zinc-700/75 my-4"></div>
                                <pre className='p-4 w-1/2'>{ex.out}</pre>
                            </Card>
                        ))
                    }
                </div>
            </article>
        </Card >
    )
}