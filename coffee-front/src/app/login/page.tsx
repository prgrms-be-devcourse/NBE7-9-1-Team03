"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

export default function LoginPage() {
    const router = useRouter();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setError(null);

        setLoading(true);
        try {
            const res = await fetch("customer/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ email, password }),
            });

            const data = await safeJson(res);
            const resultCode = data?.code ?? data?.resultCode;
            if (!res.ok || resultCode !== "200") {
                const msg = data?.message || data?.msg || `로그인 실패 (${res.status})`;
                throw new Error(msg);
            }
            router.replace("/"); // 성공하면 메인 페이지로 이동
        } catch (err: any) {
            setError(err?.message ?? "로그인 중 오류가 발생했습니다.");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="min-h-[70vh] flex items-center justify-center p-4">
            <div className="w-full max-w-md rounded-2xl shadow-lg p-6 bg-white text-gray-900 dark:bg-white dark:text-gray-900">
                <h1 className="text-2xl font-semibold mb-1">로그인</h1>
                <p className="text-sm text-gray-500 mb-6">계정에 로그인하세요.</p>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <label className="block">
                        <span className="block text-sm font-medium mb-1 text-gray-700">이메일</span>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="example@email.com"
                            required
                            className="w-full rounded-xl border px-3 py-2 outline-none focus:ring-2 focus:ring-black/10 bg-white text-gray-900 placeholder:text-gray-400"
                        />
                    </label>

                    <label className="block">
                        <span className="block text-sm font-medium mb-1 text-gray-700">비밀번호</span>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="비밀번호"
                            required
                            minLength={8}
                            className="w-full rounded-xl border px-3 py-2 outline-none focus:ring-2 focus:ring-black/10 bg-white text-gray-900 placeholder:text-gray-400"
                        />
                    </label>

                    {error && <p className="text-red-600 text-sm">{error}</p>}

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full rounded-xl bg-black text-white py-2.5 text-sm font-medium hover:opacity-90 disabled:opacity-50"
                    >
                        {loading ? "로그인 중..." : "로그인"}
                    </button>
                </form>

                <p className="mt-6 text-center text-sm text-gray-500">
                    계정이 없나요? <a className="underline" href="/join">회원가입</a>
                </p>
            </div>
        </div>
    );
}

async function safeJson(res: Response) {
    try {
        return await res.json();
    } catch {
        return null;
    }
}




