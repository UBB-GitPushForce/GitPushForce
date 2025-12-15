// src/components/Support.tsx
import React from 'react';
import '../App.css';

const Support: React.FC<{ navigate?: (to: string) => void }> = ({ navigate }) => {
    return (
        <>
            <div className="bp-title" style={{ marginTop: 12 }}>Support</div>

            <div style={{ marginTop: 12, display: 'flex', flexDirection: 'column', gap: 10 }}>
                <div style={{ color: 'var(--muted-dark)' }}>
                    If you need help, contact us using one of the options below.
                </div>

                <div style={{ color: 'var(--muted-dark)' }}>
                    <div style={{ marginTop: 8 }}><strong>Phone:</strong> +1 (555) 123-4567</div>
                    <div style={{ marginTop: 4 }}><strong>Email:</strong> support@example.com</div>
                </div>
            </div>
        </>
    );
};

export default Support;

