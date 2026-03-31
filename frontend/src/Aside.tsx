export default function Aside() {
    return (
        <aside
            className="group transition-all duration-300 overflow-hidden w-18 hover:w-60 sticky top-0 py-10 pl-5.5"
        >
            <ul className='space-y-6'>
                <li>
                    <a href="" className='flex items-center text-xl hover:text-laranja transition-color duration-300'>
                        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24">
                            <path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3q1 4 4 6.5t3 5.5a1 1 0 0 1-14 0a5 5 0 0 1 1-3a1 1 0 0 0 5 0c0-2-1.5-3-1.5-5q0-2 2.5-4" />
                        </svg>
                        <span className='max-w-0 whitespace-nowrap group-hover:max-w-50 group-hover:ml-2 overflow-hidden transition-all duration-300 font-bold'>Trilhas</span>
                    </a>
                </li>
                <li>
                    <a href="" className='flex items-center text-xl hover:text-laranja transition-color duration-300'>
                        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24">
                            <path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 14a1 1 0 0 1-.78-1.63l9.9-10.2a.5.5 0 0 1 .86.46l-1.92 6.02A1 1 0 0 0 13 10h7a1 1 0 0 1 .78 1.63l-9.9 10.2a.5.5 0 0 1-.86-.46l1.92-6.02A1 1 0 0 0 11 14z" />
                        </svg>
                        <span className='max-w-0 whitespace-nowrap group-hover:max-w-50 group-hover:ml-2 overflow-hidden transition-all duration-300 font-bold'>Contests</span>
                    </a>
                </li>
                <li>
                    <a href="" className='flex items-center text-xl hover:text-laranja transition-color duration-300'>
                        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24">
                            <g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2">
                                <rect width="4" height="6" x="14" y="14" rx="2" />
                                <rect width="4" height="6" x="6" y="4" rx="2" />
                                <path d="M6 20h4m4-10h4M6 14h2v6m6-16h2v6" />
                            </g>
                        </svg>
                        <span className='max-w-0 whitespace-nowrap group-hover:max-w-50 group-hover:ml-2 overflow-hidden transition-all duration-300 font-bold'>Problemas</span>
                    </a>
                </li>
                <li>
                    <a href="" className='flex items-center text-xl hover:text-laranja transition-color duration-300'>
                        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24">
                            <g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2">
                                <path d="m15.477 12.89l1.515 8.526a.5.5 0 0 1-.81.47l-3.58-2.687a1 1 0 0 0-1.197 0l-3.586 2.686a.5.5 0 0 1-.81-.469l1.514-8.526" />
                                <circle cx="12" cy="8" r="6" />
                            </g>
                        </svg>
                        <span className='max-w-0 whitespace-nowrap group-hover:max-w-50 group-hover:ml-2 overflow-hidden transition-all duration-300 font-bold'>Ranking</span>
                    </a>
                </li>
                <li>
                    <a href="" className='flex items-center text-xl hover:text-laranja transition-color duration-300'>
                        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24">
                            <path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h10v10M7 17L17 7" />
                        </svg>
                        <span className='max-w-0 whitespace-nowrap group-hover:max-w-50 group-hover:ml-2 overflow-hidden transition-all duration-300 font-bold'>Submissões</span>
                    </a>
                </li>
                <li>
                    <a href="" className='flex items-center text-xl hover:text-laranja transition-color duration-300'>
                        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24">
                            <path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 3a2 2 0 0 1 2 2v15a1 1 0 0 1-1.496.868l-4.512-2.578a2 2 0 0 0-1.984 0l-4.512 2.578A1 1 0 0 1 5 20V5a2 2 0 0 1 2-2z" />
                        </svg>
                        <span className='max-w-0 whitespace-nowrap group-hover:max-w-50 group-hover:ml-2 overflow-hidden transition-all duration-300 font-bold'>Salvos</span>
                    </a>
                </li>
                <li>
                    <a href="" className='flex items-center text-xl hover:text-laranja transition-color duration-300'>
                        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24">
                            <g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2">
                                <rect width="20" height="5" x="2" y="3" rx="1" />
                                <path d="M4 8v11a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8m-10 4h4" />
                            </g>
                        </svg>
                        <span className='max-w-0 whitespace-nowrap group-hover:max-w-50 group-hover:ml-2 overflow-hidden transition-all duration-300 font-bold'>Arquivo</span>
                    </a>
                </li>
            </ul >
        </aside >
    )
}