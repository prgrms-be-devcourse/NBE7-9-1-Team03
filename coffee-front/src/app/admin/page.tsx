"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { fetchApi } from "@/lib/client";
import type { ProductDto } from "@/type/product";

export default function ProductListPage() {
  const router = useRouter();
  const [items, setItems] = useState<ProductDto[] | null>(null);
  const [err, setErr] = useState<string | null>(null);

  // 🔒 관리자 접근 제어
  useEffect(() => {
    const role = parseInt(localStorage.getItem("role") || "0");
    if (role !== 1) {
      alert("관리자 권한이 필요합니다.");
      router.replace("/home"); // 일반 페이지로 이동
    }
  }, [router]);

  const load = () => {
    fetchApi<ProductDto[]>("/api/products")
      .then(setItems)
      .catch(e => setErr(e.message || "목록 조회 실패"));
  };

  useEffect(load, []);

  const onDelete = async (id: number) => {
    if (!confirm("삭제할까요?")) return;
    try {
      await fetchApi(`/api/products/${id}`, { method: "DELETE" });
      load(); // 삭제 후 새로고침
    } catch (e: any) {
      alert(e.message || "삭제 실패");
    }
  };

  return (
    <main className="p-4 max-w-3xl mx-auto">
      <header className="flex justify-end gap-3 mb-6">
        <button
          className="px-4 py-2 border rounded hover:bg-gray-50"
          onClick={() => {
            localStorage.removeItem("role"); // 로그아웃 시 role 삭제
            router.push("/logout");
          }}
        >
          로그아웃
        </button>
      </header>

      <div className="flex items-center justify-between mb-4">
        <h1 className="text-2xl font-bold">상품 관리</h1>
        <Link href="/admin/new" className="border rounded px-3 py-2">추가</Link>
      </div>

      {err && <div className="text-red-600">{err}</div>}
      {items === null && !err && <div>Loading...</div>}
      {items?.length === 0 && <div>상품이 없습니다.</div>}

      {items && items.length > 0 && (
        <ul className="divide-y">
          {items.map(p => (
            <li key={p.id} className="py-3 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <img
                  src={p.imageUrl || "/images/placeholder-product.png"}
                  alt={p.name}
                  loading="lazy"
                  className="w-20 h-20 object-cover rounded border"
                  onError={(e) => {
                    e.currentTarget.src = "/images/placeholder-product.png";
                  }}
                />
                <div>
                  <div className="font-semibold">{p.name}</div>
                  <div className="text-sm text-gray-600">
                    ₩{p.price.toLocaleString()} · 재고 {p.stock}
                  </div>
                </div>
              </div>

              <div className="flex gap-2">
                <Link href={`/admin/${p.id}/edit`} className="border rounded px-2 py-1">수정</Link>
                <button onClick={() => onDelete(p.id)} className="border rounded px-2 py-1">삭제</button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </main>
  );
}
