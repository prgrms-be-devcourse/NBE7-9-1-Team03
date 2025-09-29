import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
    async rewrites() {
        return [
            {
                // /api/... → http://localhost:8080/...
                source: '/api/:path*',
                destination: `http://localhost:8080/:path*`,
            },
        ]
    },
}

export default nextConfig