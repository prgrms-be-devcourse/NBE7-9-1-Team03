"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

export default function LoginPage() {
    const router = useRouter();
    const [email, setEmail] = useState("");      // ✅ 이메일로 로그인
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setError(null);

        setLoading(true);
        try {
            const res = await fetch("/api/customer/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ email, password }),
            });

            if (!res.ok) {
                const data = await safeJson(res);
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
        <div className="max-w-sm mx-auto mt-20 p-6 border rounded-lg shadow">
            <h1 className="text-2xl font-bold mb-6">로그인</h1>

            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                <label className="flex flex-col gap-1">
                    <span className="text-sm font-medium">이메일</span>
                    <input
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}  // ✅ 이메일 상태 업데이트
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
                        onChange={(e) => setPassword(e.target.value)} // ✅ 비밀번호 상태 업데이트
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




