import type { ChangeEventHandler } from "react";

interface Props {
    label: string;
    value: string;
    checked: boolean;
    onChange: ChangeEventHandler<HTMLInputElement, HTMLInputElement>;
}

export default function LangInput({ label, value, checked, onChange }: Props) {
    return (
        <label className="relative">
            <input
                type="radio"
                name="lang"
                value={value}
                checked={checked}
                onChange={onChange}
                className="peer hidden"
            />
            <div className="px-4 py-2 font-bold bg-zinc-800/10 border border-zinc-700/75 shadow-bear rounded-xl cursor-pointer
                      peer-checked:border-laranja peer-checked:text-laranja peer-checked:bg-laranja/10 hover:border-laranja hover:text-laranja hover:bg-laranja/10 transition-all duration-300">
                {label}
            </div>
        </label>
    )
}