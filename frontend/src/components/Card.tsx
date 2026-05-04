import { ReactNode } from 'react';
import { cn } from '../utils/classNames';

interface CardProps {
  className?: string;
  children: ReactNode;
}

export const Card = ({ className, children }: CardProps) => {
  return (
    <div className={cn('bg-white rounded-2xl shadow-[0_2px_8px_rgba(0,0,0,0.08)] p-6', className)}>
      {children}
    </div>
  );
};
