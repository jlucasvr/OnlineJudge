interface Props {
    children?: React.ReactNode;
    className?: string;
}

export default function Card({ children, className }: Props) {
    return (
        <div className={`shadow-md shadow-zinc-800 bg-zinc-900 rounded-3xl overflow-hidden border border-zinc-700 ${className}`} >
            {children}
        </div >
    )
}