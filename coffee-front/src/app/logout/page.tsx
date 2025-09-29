"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

const API = process.env.NEXT_PUBLIC_API_BASE ?? "/api";

export default function LogoutPage() {
    const [status, setStatus] = useState<"loading" | "success" | "error">("loading");
    const [msg, setMsg] = useState<string>("");

    useEffect(() => {
        (async () => {
            try {
                const res = await fetch(`${API}/customer/logout`, {
                    method: "DELETE",
                    credentials: "include",
                    headers: { Accept: "application/json" },
                });

                const ct = res.headers.get("content-type") || "";
                const body = ct.includes("application/json") ? await res.json() : await res.text();
                const code = typeof body === "object" ? (body.resultCode ?? body.code) : undefined;

                if (!res.ok || code !== "200") {
                    const reason =
                        typeof body === "object"
                            ? body?.message || body?.msg || `로그아웃 실패 (${res.status})`
                            : `로그아웃 실패 (${res.status})`;
                    throw new Error(reason);
                }
                
                setMsg(typeof body === "object" ? body?.msg || "로그아웃 되었습니다." : "로그아웃 되었습니다.");
                setStatus("success");

            } catch (e: any) {
                setMsg(e?.message ?? "로그아웃 중 오류가 발생했습니다.");
                setStatus("error");
            }
        })();
    }, []);

    return (
        <div className="min-h-[60vh] flex items-center justify-center p-6">
            <div className="w-full max-w-md rounded-2xl shadow-lg p-6 bg-white text-gray-900">
                <h1 className="text-2xl font-semibold mb-4">로그아웃</h1>

                {status === "loading" && <p>로그아웃 중...</p>}

                {status === "success" && (
                    <>
                        <p className="mb-4 text-green-600">{msg || "로그아웃 되었습니다."}</p>
                        <Link href="/" className="underline">
                            로그인 페이지로 돌아가기
                        </Link>
                    </>
                )}

                {status === "error" && (
                    <>
                        <p className="mb-4 text-red-600">{msg}</p>
                        <div className="flex gap-3">
                            <button className="border px-3 py-2 rounded" onClick={() => location.reload()}>
                                다시 시도
                            </button>
                            <Link href="/" className="underline">
                                로그인 페이지로
                            </Link>
                        </div>
                    </>
                )}
            </div>
        </div>
    );
}
