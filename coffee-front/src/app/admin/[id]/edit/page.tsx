"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { fetchApi } from "@/lib/client";
import type { ProductDto, UpdateProductDto } from "@/type/product";

export default function ProductEditPage() {
  const { id } = useParams();
  const router = useRouter();
  const [form, setForm] = useState<UpdateProductDto | null>(null);

  useEffect(() => {
    fetchApi<ProductDto>(`/api/products/${id}`).then(p => {
      setForm({ name: p.name, price: p.price, stock: p.stock, imageUrl: p.imageUrl ?? "" });
    }).catch(e => alert(e.message || "조회 실패"));
  }, [id]);

  const onChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!form) return;
    const { name, value } = e.target;
    setForm({ ...form, [name]: name === "price" || name === "stock" ? Number(value ?? 0) : value });
  };

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form) return;
    if (!form.name.trim()) return alert("상품명 입력");
    if (form.price < 0 || form.stock < 0) return alert("0 이상 입력");
    try {
      await fetchApi(`/api/products/${id}`, { method: "PUT", body: JSON.stringify(form) });
      alert("수정 완료");
      router.replace("/admin");
    } catch (e: any) {
      alert(e.message || "수정 실패");
    }
  };

  if (!form) return <main className="p-4">Loading...</main>;

  return (
    <main className="p-4 max-w-xl mx-auto">
      <h1 className="text-2xl font-bold mb-4">상품 수정</h1>
      <form className="flex flex-col gap-3" onSubmit={onSubmit}>
        <input className="border rounded p-2" name="name" placeholder="상품명" value={form.name} onChange={onChange}/>
        <input className="border rounded p-2" name="price" placeholder="가격" value={form.price} onChange={onChange}/>
        <input className="border rounded p-2" name="stock"  placeholder="재고" value={form.stock} onChange={onChange}/>
        <input className="border rounded p-2" name="imageURL" placeholder="이미지 URL(선택)" value={form.imageUrl ?? ""} onChange={onChange}/>
        <div className="flex gap-2">
          <button className="bg-blue-600 text-white rounded px-4 py-2">저장</button>
          <button type="button" className="border rounded px-4 py-2" onClick={() => router.back()}>취소</button>
        </div>
      </form>
    </main>
  );
}
