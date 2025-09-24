import { Product } from '@/lib/types'

export const dynamic = 'force-dynamic'

const API_ORIGIN = process.env.BACKEND_ORIGIN ?? 'http://localhost:8080' // 절대 URL

async function fetchProducts(): Promise<Product[]> {
  const r = await fetch(`${API_ORIGIN}/products`, { cache: 'no-store' })
  if (!r.ok) return []
  return r.json()
}

export default async function ProductsPage() {
  const items = await fetchProducts()
  return (
    <main className="p-6 max-w-5xl mx-auto">
      <h1 className="text-2xl font-bold mb-4">상품 목록</h1>
      {items.length === 0 ? (
        <p>상품이 없습니다.</p>
      ) : (
        <ul className="space-y-2">
          {items.map((p) => (
            <li key={p.id}>
              <a className="underline" href={`/products/${p.id}`}>{p.name}</a>
              {' — '}{p.price.toLocaleString()}원
            </li>
          ))}
        </ul>
      )}
    </main>
  )
}
