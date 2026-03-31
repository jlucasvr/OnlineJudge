import './App.css'
import Aside from './Aside';
import Footer from './Footer';
import Header from './Header';

interface Props {
    children?: React.ReactNode;
}

export default function Layout({ children }: Props) {
    return (
        <div className='grid grid-rows-[auto_1fr] grid-cols-[auto_1fr] relative w-full min-h-screen bg-zinc-900 text-zinc-50 leading-relaxed'>
            <Header />
            <Aside />
            <div>
                <main className='p-10 flex space-x-10 overflow-y-auto rounded-l-[48px] bg-zinc-950/50 border border-r-0 border-zinc-700/75 shadow-bear-inner'>
                    {children}
                </main >
                <Footer />
            </div>
        </div >
    );
}