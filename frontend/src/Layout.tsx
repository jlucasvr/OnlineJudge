import './App.css'

interface Props {
    children?: React.ReactNode;
}

export default function Layout({ children }: Props) {
    return (
        <div className='grid grid-rows-[auto_1fr] grid-cols-[auto_1fr] relative w-full min-h-screen bg-zinc-900 text-zinc-50'>

            <header className='h-18 col-span-2 flex items-center space-x-4 py-4 px-6'>
                <span className='text-3xl text-laranja font-black'>Pumpk<span className='text-roxo'>in</span> Code</span>
            </header>

            <aside
                className="group transition-all duration-300 overflow-hidden w-18 hover:w-50 sticky top-0 flex flex-col items-center p-4"
            >
            </aside>

            <div>
                <main className='p-10 flex space-x-10 overflow-y-auto rounded-l-[48px] bg-zinc-950'>
                    {children}
                </main >

                <footer className='min-h-18'>
                </footer>
            </div>
        </div >
    );
}