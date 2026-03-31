export default function Aside() {
    return (
        <aside
            className="group transition-all duration-300 overflow-hidden w-18 hover:w-60 sticky top-0 py-10 pl-5.5"
        >
            <ul className='space-y-6'>
                <li>
                    <a href="" className='flex items-center text-xl hover:text-laranja transition-color duration-300'>
                        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24">
                            <g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2">
                                <rect width="20" height="5" x="2" y="3" rx="1" />
                                <path d="M4 8v11a2 2 0 0 0 2 2h2M20 8v11a2 2 0 0 1-2 2h-2m-7-6l3-3l3 3m-3-3v9" />
                            </g>
                        </svg>
                        <span className='max-w-0 whitespace-nowrap group-hover:max-w-50 group-hover:ml-2 overflow-hidden transition-all duration-300 font-bold'>Arquivo</span>
                    </a>
                </li>
            </ul >
        </aside >
    )
}