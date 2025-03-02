import React, { useState } from 'react';
import * as Styled from './InputField.styles';
import clearIcon from '@/assets/images/delete_icon.png';

interface CustomInputProps {
  placeholder?: string;
  width?: string;
  maxLength?: number;
  type?: 'text' | 'password';
  label?: string;
  showClearButton?: boolean;
  showMaxChar?: boolean;
  disabled?: boolean;
  value?: string;
  onChange?: (e: React.ChangeEvent<HTMLInputElement>) => void;
  onClear?: () => void;
}

const InputField = ({
                      placeholder = '입력하세요',
                      width = '100%',
                      maxLength,
                      type = 'text',
                      label,
                      showClearButton = true,
                      showMaxChar = false,
                      disabled = false,
                      value = '',
                      onChange,
                      onClear,
                    }: CustomInputProps) => {
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);

  const togglePasswordVisibility = () => {
    if (!disabled) {
      setIsPasswordVisible(!isPasswordVisible);
    }
  };

  const clearInput = () => {
    if (!disabled && onClear) {
      onClear();
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const inputValue = e.target.value;
    if (maxLength !== undefined && inputValue.length > maxLength) {
      return;
    }
    if (onChange) {
      onChange(e);
    }
  };

  return (
      <Styled.InputContainer width={width}>
        {label && <Styled.Label>{label}</Styled.Label>}
        <Styled.InputWrapper>
          <Styled.Input
              type={type === 'password' && !isPasswordVisible ? 'password' : 'text'}
              value={value}
              onChange={handleChange}
              placeholder={placeholder}
              maxLength={maxLength}
              disabled={disabled}
          />
          {showClearButton && !disabled && (
              <Styled.ClearButton type="button" onClick={clearInput}>
                <img src={clearIcon} alt="삭제" />
              </Styled.ClearButton>
          )}
          {type === 'password' && !disabled && (
              <Styled.ToggleButton type="button" onClick={togglePasswordVisibility}>
                {isPasswordVisible ? '숨기기' : '보기'}
              </Styled.ToggleButton>
          )}
          {showMaxChar && maxLength !== undefined && (
              <Styled.CharCount>
                {value.length}/{maxLength}
              </Styled.CharCount>
          )}
        </Styled.InputWrapper>
      </Styled.InputContainer>
  );
};

export default InputField;
