"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

const API = process.env.NEXT_PUBLIC_API_BASE ?? "/api";
const EMAIL_RE = new RegExp("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

export default function QuitPage() {
    const router = useRouter();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setError(null);

        if (!EMAIL_RE.test(email)) {
            setError("올바른 이메일 형식이 아닙니다.");
            return;
        }
        if (password.length < 8) {
            setError("비밀번호는 8자 이상이어야 합니다.");
            return;
        }

        setLoading(true);
        try {
            const res = await fetch(`${API}/customer/quit`, {
                method: "DELETE",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ email, password }),
            });

            const data = await safeJson(res);
            const code = data?.resultCode ?? data?.code;

            if (!res.ok || code !== "200") {
                const msg = data?.message || data?.msg || `회원 탈퇴 실패 (${res.status})`;
                throw new Error(msg);
            }
            router.replace("/quit/done");
        } catch (err: any) {
            setError(err?.message ?? "회원 탈퇴 중 오류가 발생했습니다.");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="min-h-[70vh] flex items-center justify-center p-4">
            <div className="w-full max-w-md rounded-2xl shadow-lg p-6 bg-white text-gray-900">
                <h1 className="text-2xl font-semibold mb-1">회원 탈퇴</h1>
                <p className="text-sm text-gray-500 mb-6">
                    안전을 위해 이메일과 비밀번호를 다시 입력해 주세요.
                </p>

                {error && (
                    <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label htmlFor="email" className="block text-sm font-medium mb-1 text-gray-700">
                            이메일
                        </label>
                        <input
                            id="email"
                            type="text"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="w-full rounded-xl border px-3 py-2 outline-none focus:ring-2 focus:ring-black/10 bg-white text-gray-900 placeholder:text-gray-400"
                            placeholder="you@example.com"
                            required
                            autoComplete="email"
                        />
                    </div>

                    <div>
                        <label htmlFor="password" className="block text-sm font-medium mb-1 text-gray-700">
                            비밀번호
                        </label>
                        <input
                            id="password"
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="w-full rounded-xl border px-3 py-2 outline-none focus:ring-2 focus:ring-black/10 bg-white text-gray-900 placeholder:text-gray-400"
                            placeholder="비밀번호"
                            required
                            autoComplete="current-password"
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full rounded-xl bg-black text-white py-2.5 text-sm font-medium hover:opacity-90 disabled:opacity-50"
                    >
                        {loading ? "처리 중..." : "회원 탈퇴"}
                    </button>
                </form>

                <p className="mt-6 text-center text-sm text-gray-500">
                    취소하시겠어요? <a className="underline" href="/home">메인화면으로</a>
                </p>
            </div>
        </div>
    );
}

async function safeJson(res: Response) {
    try { return await res.json(); } catch { return null; }
}
