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

        const emailOk =
            /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email) &&
            /@(naver\.com|gmail\.com|daum\.net|nate\.com|hanmail\.net|kakao\.com)$/i.test(email);
        if (!emailOk) {
            setError("올바른 이메일 형식이 아닙니다.");
            return;
        }

        setLoading(true);
        try {
            const res = await fetch("/customer/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ email, password }),
            });

            const data = await safeJson(res);
            const resultCode = data?.resultCode ?? data?.code;

            if (!res.ok || resultCode !== "200") {
                const msg = data?.message || data?.msg || `로그인 실패 (${res.status})`;
                throw new Error(msg);
            }

            router.replace("/");
        } catch (err: any) {
            setError(err?.message ?? "로그인 중 오류가 발생했습니다.");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="max-w-sm mx-auto mt-20 p-6 border rounded-lg shadow">
            <h1 className="text-2xl font-bold mb-6">로그인</h1>

            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                <label className="flex flex-col gap-1">
                    <span className="text-sm font-medium">이메일</span>
                    <input
                        type="text"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="example@email.com"
                        required
                        className="border rounded px-3 py-2"
                    />
                </label>

                <label className="flex flex-col gap-1">
                    <span className="text-sm font-medium">비밀번호</span>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="비밀번호"
                        required
                        minLength={8}
                        className="border rounded px-3 py-2"
                    />
                </label>

                {error && <p className="text-red-600 text-sm">{error}</p>}

                <button
                    type="submit"
                    disabled={loading}
                    className="bg-blue-500 text-white rounded px-3 py-2 hover:bg-blue-600"
                >
                    {loading ? "로그인 중..." : "로그인"}
                </button>
            </form>
            <p className="mt-6 text-center text-sm text-gray-500">
                계정이 없나요? <a className="underline" href="/join">회원가입</a>
            </p>
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




