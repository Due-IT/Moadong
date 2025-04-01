import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import * as Styled from './LoginTab.styles';
import InputField from '@/components/common/InputField/InputField';
import Button from '@/components/common/Button/Button';
import { login } from '@/apis/auth/login';
import moadong_name_logo from '@/assets/images/logos/moadong_name_logo.svg';
import useAuth from '@/hooks/useAuth'; // 여기 추가!

const LoginTab = () => {
  const [userId, setUserId] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const { isAuthenticated, isLoading: authLoading } = useAuth();

  useEffect(() => {
    if (!authLoading && isAuthenticated) {
      navigate('/admin', { replace: true });
    }
  }, [authLoading, isAuthenticated, navigate]);

  const handleLogin = async () => {
    if (loading) return;
    setLoading(true);
    try {
      const { accessToken } = await login(userId, password);
      localStorage.setItem('accessToken', accessToken);
      alert('로그인 성공! 관리자 페이지로 이동합니다.');
      navigate('/admin');
    } catch (error: unknown) {
      console.error('로그인 실패:', error);
      let errorMessage =
        '로그인에 실패했습니다. 아이디 또는 비밀번호를 확인해주세요.';
      if (error instanceof Error) {
        errorMessage = error.message;
      }
      alert(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  if (authLoading) return <div>로딩 중...</div>;

  return (
    <Styled.LoginContainer>
      <Styled.LoginBox>
        <Styled.Logo src={moadong_name_logo} alt='Moadong Logo' />
        <Styled.Title>Log in</Styled.Title>
        <Styled.InputFieldsContainer>
          <InputField
            type='text'
            placeholder='아이디'
            showClearButton={false}
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
          />
          <InputField
            type='password'
            placeholder='비밀번호'
            showClearButton={false}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </Styled.InputFieldsContainer>

        <Styled.ButtonWrapper>
          <Button width='100%' onClick={handleLogin}>
            {loading ? '로그인 중...' : '로그인'}
          </Button>
        </Styled.ButtonWrapper>

        <Styled.ForgotLinks>
          <Link to=''>회원가입</Link>
          <span>|</span>
          <Link to=''>아이디 찾기</Link>
          <span>|</span>
          <Link to=''>비밀번호 찾기</Link>
        </Styled.ForgotLinks>
      </Styled.LoginBox>
    </Styled.LoginContainer>
  );
};

export default LoginTab;
