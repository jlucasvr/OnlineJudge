interface Props {
    children?: React.ReactNode;
    className?: string;
}

export default function Card({ children, className }: Props) {
    return (
        <div className={`shadow-bear bg-zinc-900 rounded-3xl overflow-hidden border border-zinc-700/75 ${className}`} >
            {children}
        </div >
    )
}