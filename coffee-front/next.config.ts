import type { NextConfig } from 'next'

const BACKEND = process.env.BACKEND_ORIGIN ?? 'http://localhost:8080'

const nextConfig: NextConfig = {
    async rewrites() {
        return [
            {
                // /api/... → http://localhost:8080/...
                source: '/api/:path*',
                destination: `${process.env.BACKEND_ORIGIN ?? 'http://localhost:8080'}/:path*`,
            },
            {
                // /customer/... → http://localhost:8080/customer/...
                source: '/customer/:path*',
                destination: `${BACKEND}/customer/:path*`,
            },
        ]
    },
}

export default nextConfig