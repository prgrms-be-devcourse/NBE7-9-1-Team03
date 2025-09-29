"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { fetchApi } from "@/lib/client";
import type { ProductDto } from "@/type/product";
import Link from "next/link";

export default function ProductDetailPage() {
  const { id } = useParams();
  const router = useRouter();
  const [product, setProduct] = useState<ProductDto | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchApi<ProductDto>(`/api/products/${id}`)
      .then(setProduct)
      .catch((e) => {
        setError(e.message || "상품 조회 실패");
      });
  }, [id]);

  const onDelete = async () => {
    if (!confirm("정말 삭제하시겠습니까?")) return;
    try {
      // 백엔드가 DELETE /products/{id}인지, DELETE /products + body 인지 팀 표준에 맞춰 변경
      await fetchApi(`/api/products/${id}`, { method: "DELETE" });
      alert("삭제되었습니다.");
      router.replace("/admin");
    } catch (e: any) {
      alert(e.message || "삭제 실패");
    }
  };

  if (error) return <main className="p-4">{error}</main>;
  if (!product) return <main className="p-4">Loading...</main>;

  return (
    <main className="p-4 max-w-xl mx-auto">
      <h1 className="text-2xl font-bold mb-4">{product.name}</h1>
      <div className="mb-2">가격: ₩{product.price.toLocaleString()}</div>
      <div className="mb-2">재고: {product.stock}</div>
      {product.imageUrl && (
        <img src={product.imageUrl} alt={product.name} className="max-w-full border rounded mb-4"/>
      )}
      <div className="flex gap-2">
        <Link href={`/admin/${product.id}/edit`} className="border rounded px-3 py-2">수정</Link>
        <button className="border rounded px-3 py-2" onClick={onDelete}>삭제</button>
        <Link href="/admin" className="border rounded px-3 py-2">목록</Link>
      </div>
    </main>
  );
}
