// src/components/ThemeToggle.tsx
import React from 'react';
import { useTheme } from '../contexts/ThemeContext';

const ThemeToggle: React.FC = () => {
    const { theme, toggle } = useTheme();

    return (
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <div style={{ fontSize: 12, color: theme === 'dark' ? '#fff' : '#3c3c78' }}>
                {theme === 'dark' ? 'Dark' : 'Light'}
            </div>

            <label className="theme-toggle" aria-label="Theme toggle">
                <input
                    type="checkbox"
                    checked={theme === 'dark'}
                    onChange={toggle}
                />
                <span className="slider" />
            </label>
        </div>
    );
};

export default ThemeToggle;

